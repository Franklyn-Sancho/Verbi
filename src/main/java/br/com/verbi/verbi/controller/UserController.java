package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;

import br.com.verbi.verbi.dto.LoginDto;
import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.TokenExpiredException;
import br.com.verbi.verbi.exception.TokenInvalidException;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.EmailService;
import br.com.verbi.verbi.service.TokenBlacklistService;
import br.com.verbi.verbi.service.UserService;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> registerUser(
            @RequestPart("userDto") @Valid @ModelAttribute UserDto userDto,
            @RequestPart(value = "picture", required = false) MultipartFile picture) {
        String message = userService.registerUserWithEmail(userDto, picture);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        // Verifique se o usuário existe
        Optional<User> userOptional = userService.findUserByEmail(loginDto.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Se o usuário estiver suspenso, reative a conta e retorne um token
            if (user.isSuspended()) {
                userService.reactivateUser(user.getId()); // Reativa a conta
            }
        }

        boolean isAuthenticated = userService.authenticateUser(loginDto.getEmail(), loginDto.getPassword());
        if (isAuthenticated) {
            String token = jwtGenerator.generateToken(loginDto.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("token", "Bearer " + token);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7);
        tokenBlacklistService.blacklistToken(actualToken);
        return ResponseEntity.ok("Logout Successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        Optional<User> user = userService.findUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findUserByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<User> findUsersByName(@RequestParam String name) {
        return userService.findUserByName(name);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        try {
            User updatedUser = userService.updateUser(id, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/password/request-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestBody UserDto userDto) {
        try {
            User newUser = userService.requestPasswordReset(userDto.getEmail());

            String resetLink = "http://localhost:8080/reset-password?token=" + newUser.getResetPasswordToken();

            emailService.sendResetPasswordEmail(newUser, resetLink);
            return ResponseEntity.ok("Password reset email sent.");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody UserDto userDto) {
        try {
            userService.resetPassword(token, userDto.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (TokenExpiredException e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (TokenInvalidException e) {
            System.err.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PutMapping("/password/update")
    public ResponseEntity<String> updatePassword(@RequestParam UUID userId,
            @RequestParam String oldPassword,
            @RequestBody String newPassword) {
        userService.updatePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok("Password updated successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable UUID id) {
        userService.suspendUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/mark-for-deletion")
    public ResponseEntity<Void> markForDeletion(@PathVariable UUID id) {
        userService.markForDeletion(id);
        return ResponseEntity.noContent().build();
    }
}

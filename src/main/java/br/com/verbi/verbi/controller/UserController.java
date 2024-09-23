package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.EmailAlreadyExistsException;
import br.com.verbi.verbi.exception.TokenExpiredException;
import br.com.verbi.verbi.exception.TokenInvalidException;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.repository.UserRepository;
import br.com.verbi.verbi.service.EmailService;
import br.com.verbi.verbi.service.FileService;
import br.com.verbi.verbi.service.UserService;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;


    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(
        @RequestPart("userDto") @Valid @ModelAttribute UserDto userDto,
        @RequestPart(value = "picture", required = false) MultipartFile picture) {
        try {
            // Lógica para registro de usuário com a imagem
            User newUser = userService.registerUser(
                    userDto.getName(),
                    userDto.getEmail(),
                    userDto.getPassword(),
                    picture);
    
            // Salva o usuário no banco de dados
            userRepository.save(newUser);
    
            // Tenta enviar o e-mail de confirmação
            try {
                String confirmationLink = "http://localhost:8080/confirm-email/" + newUser.getEmailConfirmationToken();
                emailService.sendConfirmationEmail(newUser, confirmationLink);
            } catch (Exception e) {
                System.err.println("Failed to send confirmation email: " + e.getMessage());
                // Log do erro, mas não interrompe o fluxo
            }
    
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    
        } catch (EmailAlreadyExistsException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
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
            System.err.println("Error: " + e.getMessage());
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

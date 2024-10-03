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

    /**
     * Registers a new user with an optional profile picture.
     *
     * @param userDto DTO containing user details
     * @param picture Optional profile picture
     * @return ResponseEntity with the created User or conflict status
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> registerUser(
            @RequestPart("userDto") @Valid @ModelAttribute UserDto userDto,
            @RequestPart(value = "picture", required = false) MultipartFile picture) {
        try {
            User user = userService.registerUserWithEmail(userDto, picture);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // Return conflict if email is already in use
        }
    }

    /**
     * Authenticates a user based on email and password.
     *
     * @param loginDto DTO containing login credentials
     * @return ResponseEntity containing a token if authenticated, otherwise
     *         unauthorized status
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        Optional<User> userOptional = userService.findUserByEmail(loginDto.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Reactivate account if user is suspended
            if (user.isSuspended()) {
                userService.reactivateUser(user.getId());
            }
        }

        boolean isAuthenticated = userService.authenticateUser(loginDto.getEmail(), loginDto.getPassword());
        if (isAuthenticated) {
            String token = jwtGenerator.generateToken(loginDto.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("token", "Bearer " + token);
            return ResponseEntity.ok(response); // Return token if authenticated
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized"); // Unauthorized status
    }

    /**
     * Logs out the user by blacklisting the provided token.
     *
     * @param token JWT token to be blacklisted
     * @return ResponseEntity with logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7); // Remove "Bearer " prefix
        tokenBlacklistService.blacklistToken(actualToken);
        return ResponseEntity.ok("Logout Successfully");
    }

    /**
     * Retrieves a user by their UUID.
     *
     * @param id UUID of the user
     * @return ResponseEntity with the found User or not found status
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        Optional<User> user = userService.findUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email Email of the user
     * @return ResponseEntity with the found User or not found status
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findUserByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Searches for users by their name.
     *
     * @param name Name to search for
     * @return List of users matching the search criteria
     */
    @GetMapping("/search")
    public List<User> findUsersByName(@RequestParam String name) {
        return userService.findUserByName(name);
    }

    /**
     * Updates user information based on the provided UUID.
     *
     * @param id      UUID of the user to update
     * @param userDto DTO containing new user details
     * @return ResponseEntity with the updated User or not found status
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        try {
            User updatedUser = userService.updateUser(id, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // Handle user not found
        }
    }

    /**
     * Initiates the password reset process by sending an email with a reset link.
     *
     * @param userDto DTO containing the user's email
     * @return ResponseEntity with a message regarding the email status
     */
    @PostMapping("/password/request-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestBody UserDto userDto) {
        try {
            User newUser = userService.requestPasswordReset(userDto.getEmail());
            String resetLink = "http://localhost:8080/reset-password?token=" + newUser.getResetPasswordToken();
            emailService.sendResetPasswordEmail(newUser, resetLink);
            return ResponseEntity.ok("Password reset email sent.");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Handle user not found
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    /**
     * Resets the user's password based on the provided token and new password.
     *
     * @param token   Reset token
     * @param userDto DTO containing the new password
     * @return ResponseEntity with a success message
     */
    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody UserDto userDto) {
        try {
            userService.resetPassword(token, userDto.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (TokenExpiredException | TokenInvalidException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // Handle token issues
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    /**
     * Updates the user's password.
     *
     * @param userId      UUID of the user
     * @param oldPassword Old password for verification
     * @param newPassword New password to set
     * @return ResponseEntity with a success message
     */
    @PutMapping("/password/update")
    public ResponseEntity<String> updatePassword(@RequestParam UUID userId,
            @RequestParam String oldPassword,
            @RequestBody String newPassword) {
        userService.updatePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok("Password updated successfully.");
    }

    /**
     * Deletes a user by their UUID.
     *
     * @param id UUID of the user to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // Return no content status after deletion
    }

    /**
     * Suspends a user by their UUID.
     *
     * @param id UUID of the user to suspend
     * @return ResponseEntity with no content
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable UUID id) {
        userService.suspendUser(id);
        return ResponseEntity.noContent().build(); // Return no content status after suspension
    }

    /**
     * Marks a user for deletion by their UUID.
     *
     * @param id UUID of the user to mark for deletion
     * @return ResponseEntity with no content
     */
    @PostMapping("/{id}/mark-for-deletion")
    public ResponseEntity<Void> markForDeletion(@PathVariable UUID id) {
        userService.markForDeletion(id);
        return ResponseEntity.noContent().build(); // Return no content status after marking for deletion
    }
}

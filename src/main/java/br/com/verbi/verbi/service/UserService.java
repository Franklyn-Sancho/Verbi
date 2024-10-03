package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.EmailAlreadyExistsException;
import br.com.verbi.verbi.exception.EmailServiceUnavailableException;
import br.com.verbi.verbi.exception.TokenExpiredException;
import br.com.verbi.verbi.exception.TokenInvalidException;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.repository.UserRepository;

import java.time.LocalDateTime;

import java.io.IOException;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository; // Repository for User data access

    @Autowired
    private PasswordEncoder passwordEncoder; // Encoder for password encryption

    @Autowired
    private AuthorizationService authorizationService; // Service for authorization checks

    @Autowired
    private FileService fileService; // Service for file handling

    @Autowired
    private EmailService emailService; // Service for email handling

    /**
     * Registers a new user with the provided email and picture.
     * 
     * @param userDto Data Transfer Object containing user data
     * @param picture MultipartFile representing the user's profile picture
     * @return Saved User entity
     * @throws RuntimeException if the email is already in use
     */
    public User registerUserWithEmail(UserDto userDto, MultipartFile picture) {
        // Check if the email is already in use
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Create a new User from UserDto
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Encrypt password

        // Generate confirmation token
        String confirmationToken = UUID.randomUUID().toString();
        user.setEmailConfirmationToken(confirmationToken); // Assign token to user

        // Process profile picture if present
        if (picture != null && !picture.isEmpty()) {
            try {
                String pictureUrl = fileService.saveFile(picture, "imageProfile");
                user.setPicture(pictureUrl); // Set the profile picture URL
            } catch (IOException e) {
                throw new RuntimeException("Failed to save picture: " + e.getMessage(), e);
            }
        }

        sendConfirmationEmail(user); // Send confirmation email

        // Save the user in the repository and return the User entity
        return userRepository.save(user);
    }

    /**
     * Saves the given User entity.
     * 
     * @param user User entity to save
     * @return Saved User entity
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Sends a confirmation email to the user.
     * 
     * @param user User entity to send the email to
     * @return Status message regarding email sending
     */
    private String sendConfirmationEmail(User user) {
        try {
            emailService.sendConfirmationEmail(user); // Send the email
            return "User registered successfully and confirmation email sent.";
        } catch (EmailServiceUnavailableException e) {
            return "User registered successfully, but the email service is down. The confirmation email will be sent as soon as possible.";
        } catch (Exception e) {
            return "User registered successfully, but an error occurred while sending the confirmation email.";
        }
    }

    /**
     * Authenticates a user with the provided email and password.
     * 
     * @param email    User's email
     * @param password User's password
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null); // Retrieve user by email
        if (user != null) {
            return passwordEncoder.matches(password, user.getPassword()); // Check password
        }
        return false;
    }

    /**
     * Creates a new User entity from the provided UserDto.
     * 
     * @param userDto Data Transfer Object containing user data
     * @return Created User entity
     */
    public User createUser(UserDto userDto) {
        User user = new User();
        user.setId(UUID.randomUUID()); // Assign a new UUID
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Encrypt password
        return user;
    }

    /**
     * Finds a User by their UUID.
     * 
     * @param userId User's UUID
     * @return Optional containing the User, if found
     */
    public Optional<User> findUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Finds a User by their email.
     * 
     * @param email User's email
     * @return Optional containing the User, if found
     */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds Users whose names contain the specified string.
     * 
     * @param name Name substring to search for
     * @return List of Users whose names contain the specified substring
     */
    public List<User> findUserByName(String name) {
        return userRepository.findByNameContaining(name);
    }

    /**
     * Updates the user's password after verifying the old password.
     * 
     * @param userId      UUID of the user
     * @param oldPassword Current password of the user
     * @param newPassword New password to set
     */
    public void updatePassword(UUID userId, String oldPassword, String newPassword) {
        authorizationService.verifyUserAuthorization(userId, userRepository); // Verify authorization

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password"); // Check if the old password matches
        }

        user.setPassword(passwordEncoder.encode(newPassword)); // Encrypt new password
        userRepository.save(user); // Save changes
    }

    /**
     * Updates the user's profile picture.
     * 
     * @param userId   UUID of the user
     * @param fileName New file name of the profile picture
     */
    public void updateUserPicure(UUID userId, String fileName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));

        user.setPicture(fileName); // Set new profile picture file name
        userRepository.save(user); // Save changes
    }

    /**
     * Requests a password reset by generating a reset token.
     * 
     * @param email User's email
     * @return User entity for further processing
     * @throws UserNotFoundException if no user is found with the provided email
     */
    public User requestPasswordReset(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("No user found with this email."); // No user found
        }

        User user = optionalUser.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordExpires(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour

        userRepository.save(user); // Save changes

        return user;
    }

    /**
     * Resets the user's password using the provided reset token.
     * 
     * @param token       Reset password token
     * @param newPassword New password to set
     * @throws TokenInvalidException if the token is invalid
     * @throws TokenExpiredException if the token has expired
     */
    public void resetPassword(String token, String newPassword) {
        Optional<User> optionalUser = userRepository.findByResetPasswordToken(token);

        if (!optionalUser.isPresent()) {
            throw new TokenInvalidException("Invalid reset password token."); // Invalid token
        }

        User user = optionalUser.get();

        // Verify if the token is still valid
        if (user.getResetPasswordExpires().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("The reset password token has expired."); // Token expired
        }

        // Update the password and remove the reset token
        user.setPassword(passwordEncoder.encode(newPassword)); // Encrypt new password
        user.setResetPasswordToken(null); // Clear the reset token
        user.setResetPasswordExpires(null); // Clear expiration date

        userRepository.save(user); // Save changes
    }

    /**
     * Updates the user with the given UserDto.
     * 
     * @param userId  UUID of the user
     * @param userDto Data Transfer Object containing new user data
     * @return Updated User entity
     */
    public User updateUser(UUID userId, UserDto userDto) {
        authorizationService.verifyUserAuthorization(userId, userRepository); // Verify authorization

        return userRepository.findById(userId).map(user -> {
            // Update user data
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            user.setDescription(userDto.getDescription());

            // If a new password is provided, update the password
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Encrypt new password
            }

            return userRepository.save(user); // Save changes
        }).orElseThrow(() -> new RuntimeException("User not found with id " + userId)); // Handle user not found
    }

    /**
     * Suspends a user by setting their suspended status to true.
     * 
     * @param userId UUID of the user to suspend
     */
    public void suspendUser(UUID userId) {
        authorizationService.verifyUserAuthorization(userId, userRepository); // Verify authorization

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setSuspended(true); // Set user as suspended
        user.setSuspensionDate(LocalDateTime.now()); // Set suspension date
        userRepository.save(user); // Save changes
    }

    /**
     * Reactivates a suspended user by setting their suspended status to false.
     * 
     * @param userId UUID of the user to reactivate
     */
    public void reactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));

        user.setSuspended(false); // Set user as not suspended
        user.setSuspensionDate(null); // Clear suspension date
        userRepository.save(user); // Save changes
    }

    /**
     * Checks if a user is suspended.
     * 
     * @param userId UUID of the user
     * @return true if the user is suspended, false otherwise
     */
    public boolean isUserSuspended(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found"));

        return user.isSuspended(); // Return suspended status
    }

    /**
     * Marks a user for deletion by setting the deletion mark date.
     * 
     * @param userId UUID of the user to mark for deletion
     */
    public void markForDeletion(UUID userId) {
        authorizationService.verifyUserAuthorization(userId, userRepository); // Verify authorization

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not Found"));

        user.setDeleteMarkedDate(LocalDateTime.now()); // Mark user for deletion
        userRepository.save(user); // Save changes
    }

    /**
     * Deletes accounts marked for deletion that are older than 30 days.
     * This method is scheduled to run daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?") // Executes daily at midnight
    public void deleteMarkedAccounts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffDate = now.minusDays(30); // Exclude after 30 days

        List<User> usersToDelete = userRepository.findByDeleteMarkedDateBeforeAndDeletionDateIsNull(cutoffDate);

        for (User user : usersToDelete) {
            deleteUser(user.getId()); // Delete all accounts
        }
    }

    /**
     * Deletes a user by their UUID.
     * 
     * @param userId UUID of the user to delete
     */
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not Found"));

        userRepository.delete(user); // Delete user
    }

}

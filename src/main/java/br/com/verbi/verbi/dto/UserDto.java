package br.com.verbi.verbi.dto;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UserDto {
    
    // Basic user information
    private String name;
    private String email;
    private String description;
    
    // For password handling (e.g., registration or reset)
    private String password;
    private String newPassword;

    // Email confirmation fields
    private String emailConfirmationToken;
    private LocalDateTime emailConfirmationExpires; // Changed to LocalDateTime for better date handling

    // Picture upload (multipart file)
    private MultipartFile picture;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getEmailConfirmationToken() {
        return emailConfirmationToken;
    }

    public void setEmailConfirmationToken(String emailConfirmationToken) {
        this.emailConfirmationToken = emailConfirmationToken;
    }

    public LocalDateTime getEmailConfirmationExpires() {
        return emailConfirmationExpires;
    }

    public void setEmailConfirmationExpires(LocalDateTime emailConfirmationExpires) {
        this.emailConfirmationExpires = emailConfirmationExpires;
    }

    public MultipartFile getPicture() {
        return picture;
    }

    public void setPicture(MultipartFile picture) {
        this.picture = picture;
    }
}



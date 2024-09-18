package br.com.verbi.verbi.dto;

public class UserDto {
    private String name;
    private String email;
    private String description; 
    private String password; 
    private String emailConfirmationToken;
    private String emailConfirmationExpires;

    // Getters e Setters
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

    public String getemailConfirmationToken() {
        return emailConfirmationToken;
    }

    public void setEmailConfirmationToken(String emailConfirmationToken) {
        this.emailConfirmationToken = emailConfirmationToken;
    }

    public String getEmailConfirmationExpires() {
        return emailConfirmationExpires;
    }

    public void setEmailConfirmationExpires(String emailConfirmationExpires) {
        this.emailConfirmationExpires = emailConfirmationExpires;
    }
}


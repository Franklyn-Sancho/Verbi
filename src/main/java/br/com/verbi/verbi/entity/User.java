package br.com.verbi.verbi.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.Set;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.HashSet;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String email;
    private String picture;
    private String password;
    private String description;

    @Column(nullable = false)
    private boolean suspended = false;
    private LocalDateTime suspensionDate;

    private LocalDateTime deleteMarkedDate; // Data em que a conta foi marcada para exclusão
    private LocalDateTime deletionDate; // Data em que a conta foi realmente excluída

    private String googleId;
    
    private String emailConfirmationToken;
    private LocalDateTime emailConfirmationExpires;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Mural> murals = new HashSet<>();

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public LocalDateTime getSuspensionDate() {
        return suspensionDate;
    }

    public void setSuspensionDate(LocalDateTime suspensionDate) {
        this.suspensionDate = suspensionDate;
    }

    public LocalDateTime getDeleteMarkedDate() {
        return deleteMarkedDate;
    }

    public void setDeleteMarkedDate(LocalDateTime deleteMarkedDate) {
        this.deleteMarkedDate = deleteMarkedDate;
    }

    public LocalDateTime getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(LocalDateTime deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Mural> getMurals() {
        return murals;
    }

    public void setMurals(Set<Mural> murals) {
        this.murals = murals;
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
}

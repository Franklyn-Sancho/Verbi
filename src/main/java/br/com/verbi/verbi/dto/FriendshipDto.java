package br.com.verbi.verbi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.verbi.verbi.entity.Friendship;
import br.com.verbi.verbi.enums.FriendshipStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.verbi.verbi.enums.FriendshipStatus;

public class FriendshipDto {
    
    private UUID id;
    private String senderName;
    private String receiverName;
    private FriendshipStatus status;
    private LocalDateTime createdAt;

    // Constructors
    public FriendshipDto(UUID id, String senderName, String receiverName, FriendshipStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Static factory method to convert from entity to DTO
    public static FriendshipDto fromEntity(Friendship friendship) {
        return new FriendshipDto(
            friendship.getId(),
            friendship.getSender().getName(),  // Assuming 'User' has a 'getName()' method
            friendship.getReceiver().getName(), // Assuming 'User' has a 'getName()' method
            friendship.getStatus(),
            friendship.getCreateAt()
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


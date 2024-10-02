package br.com.verbi.verbi.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import br.com.verbi.verbi.enums.FriendshipStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Friendship {

    

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonManagedReference
    private User sender; 

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonManagedReference
    private User receiver; 

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status; // Status da amizade: PENDING, ACCEPTED, DECLINED

    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public LocalDateTime getCreateAt() {
        return createdAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createdAt = createAt;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public Friendship() {
        this.createdAt = LocalDateTime.now();
        this.status = FriendshipStatus.PENDING;
    }
}

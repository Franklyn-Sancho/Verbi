package br.com.verbi.verbi.dto;

import java.util.UUID;

public class ChatDto {
    private UUID id;
    private UUID user1Id;
    private UUID user2Id;

    // Construtores
    public ChatDto() {}

    public ChatDto(UUID id, UUID user1Id, UUID user2Id) {
        this.id = id;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(UUID user1Id) {
        this.user1Id = user1Id;
    }

    public UUID getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(UUID user2Id) {
        this.user2Id = user2Id;
    }
}

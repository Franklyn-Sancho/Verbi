package br.com.verbi.verbi.dto;

import java.util.UUID;

public class CreateChatRequest {

    private UUID user1Id;
    private UUID user2Id;

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



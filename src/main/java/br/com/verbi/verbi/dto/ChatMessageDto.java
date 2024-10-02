package br.com.verbi.verbi.dto;

import java.util.UUID;

public class ChatMessageDto {
    private UUID senderId;
    private UUID receiverId;
    private String content;

    public ChatMessageDto() {}

    public ChatMessageDto(UUID senderId, UUID receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}


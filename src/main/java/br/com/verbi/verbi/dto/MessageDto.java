package br.com.verbi.verbi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.verbi.verbi.entity.Message;

public class MessageDto {
    private UUID id;
    private UUID chatId;
    private UUID senderId;
    private String content;
    private LocalDateTime timestamp;

    public MessageDto() {}

    public MessageDto(UUID id, UUID chatId, UUID senderId, String content, LocalDateTime timestamp) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Static factory method for conversion from Message entity
    public static MessageDto fromEntity(Message message) {
        return new MessageDto(
            message.getId(),
            message.getChat().getId(),
            message.getSender().getId(),
            message.getContent(),
            message.getTimestamp()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChatId() {
        return chatId;
    }

    public void setChatId(UUID chatId) {
        this.chatId = chatId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}


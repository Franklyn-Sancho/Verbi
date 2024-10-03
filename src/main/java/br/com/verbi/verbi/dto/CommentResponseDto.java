package br.com.verbi.verbi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommentResponseDto {

    private UUID id;
    private String content;
    private String authorName; // Nome do autor
    private LocalDateTime createdAt; // Data de criação

    // Construtor
    public CommentResponseDto(UUID id, String content, String authorName, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.authorName = authorName;
        this.createdAt = createdAt;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorName() {
        return authorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}


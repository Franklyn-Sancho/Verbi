package br.com.verbi.verbi.dto;

import java.time.LocalDateTime;

public class CommentDto {

    private String content;
    private LocalDateTime createdAt; // Você pode incluir isso se quiser retornar a data de criação

    // Construtores
    public CommentDto() {
    }

    public CommentDto(String content) {
        this.content = content;
    }

    // Getters e Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


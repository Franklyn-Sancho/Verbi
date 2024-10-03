package br.com.verbi.verbi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CommentDto {

    @NotBlank(message = "Content cannot be empty")
    private String content;

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
}



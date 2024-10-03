package br.com.verbi.verbi.dto;

import java.util.UUID;

import br.com.verbi.verbi.enums.MuralVisibility;
import lombok.Data;

@Data
public class MuralResponseDto {
    private UUID id;
    private String body;
    private MuralVisibility visibility;
    private String userName; // Novo campo para armazenar o nome do usuário

    // Construtor atualizado
    public MuralResponseDto(UUID id, String body, MuralVisibility visibility, String userName) {
        this.id = id;
        this.body = body;
        this.visibility = visibility;
        this.userName = userName; // Inicializa o novo campo
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public MuralVisibility getVisibility() {
        return visibility;
    }

    public String getUserName() { // Novo getter
        return userName;
    }
}



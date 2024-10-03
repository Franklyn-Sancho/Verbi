package br.com.verbi.verbi.dto;

import br.com.verbi.verbi.enums.MuralVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MuralDto {
    
    @NotBlank(message = "Body cannot be empty")
    private String body;

    @NotNull(message = "Visibility must be specified")
    private MuralVisibility visibility; // Adicionando a visibilidade

    // Getters e Setters
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public MuralVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(MuralVisibility visibility) {
        this.visibility = visibility;
    }
}



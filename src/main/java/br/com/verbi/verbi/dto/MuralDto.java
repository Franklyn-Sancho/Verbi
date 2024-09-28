package br.com.verbi.verbi.dto;

import br.com.verbi.verbi.enums.MuralVisibility;

public class MuralDto {
    private String body;
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


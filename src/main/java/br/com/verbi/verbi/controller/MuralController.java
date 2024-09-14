package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.verbi.verbi.dto.MuralDto;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.MuralService;
import br.com.verbi.verbi.service.UserService;

@RestController
@RequestMapping("api/mural")
public class MuralController {
    
    @Autowired
    private MuralService muralService;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTGenerator jwtGenerator; // Para extrair o email do JWT

    @PostMapping("/write")
    public ResponseEntity<Mural> createMural(@RequestBody MuralDto muralDto, @RequestHeader("Authorization") String token) {
        // Remove o prefixo "Bearer " do token
        String actualToken = token.substring(7);

        // Extrai o email do token JWT
        String email = jwtGenerator.getUsername(actualToken);

        // Busca o usuário pelo e-mail
        User user = userService.findUserByEmail(email)
                               .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Cria o mural associado ao usuário
        Mural mural = muralService.createMural(muralDto.getBody(), user);

        return ResponseEntity.status(HttpStatus.CREATED).body(mural);
    }
}

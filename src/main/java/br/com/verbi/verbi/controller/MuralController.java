package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

@RestController
@RequestMapping("/api/mural")
public class MuralController {

    @Autowired
    private MuralService muralService;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTGenerator jwtGenerator; // Para extrair o email do JWT

    @PostMapping("/write")
    public ResponseEntity<Mural> createMural(@RequestBody MuralDto muralDto,
            @RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7);

        String email = jwtGenerator.getUsername(actualToken);

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        Mural mural = muralService.createMural(muralDto.getBody(), user);

        return ResponseEntity.status(HttpStatus.CREATED).body(mural);
    }

    @GetMapping("/user/{name}")
    public Page<Mural> getMuralsByUserName(@PathVariable String name, Pageable pageable) {
        return muralService.findMuralsByUserName(name, pageable);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Mural> updateMural(@PathVariable UUID id, 
                                             @RequestBody MuralDto muralDto, 
                                             @RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.substring(7); // Remove o "Bearer " do token
            String email = jwtGenerator.getUsername(actualToken); // Extrai o email do token
    
            User user = userService.findUserByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    
            Mural updatedMural = muralService.updateMural(id, muralDto, user);
            return ResponseEntity.ok(updatedMural);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMural(@PathVariable UUID id, @RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7); // Remover o prefixo "Bearer "
        String email = jwtGenerator.getUsername(actualToken); // Obter o email do token
    
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    
        muralService.deleteMural(id, user);
    
        return ResponseEntity.noContent().build();
    }
    
}

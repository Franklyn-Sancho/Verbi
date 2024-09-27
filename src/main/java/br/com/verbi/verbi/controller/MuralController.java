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
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.MuralService;
import br.com.verbi.verbi.service.UserService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

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

    // Método privado para simplificar extração de usuário a partir do token
    private User extractUserFromToken(String token) {
        String actualToken = token.substring(7); // Remove o "Bearer " do token
        String email = jwtGenerator.getUsername(actualToken); // Extrai o email do token
        return userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @PostMapping("/write")
    public ResponseEntity<Mural> createMural(@RequestBody MuralDto muralDto,
            @RequestHeader("Authorization") String token) {
        User user = extractUserFromToken(token);

        Mural mural = muralService.createMural(muralDto.getBody(), user);

        return ResponseEntity.status(HttpStatus.CREATED).body(mural);
    }

    @GetMapping("/user/{name}")
    public ResponseEntity<Page<Mural>> getMuralsByUserName(@PathVariable String name,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Mural> murals = muralService.findMuralsByUserName(name, pageable);
        return ResponseEntity.ok(murals);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Mural> updateMural(@PathVariable UUID id,
            @RequestBody MuralDto muralDto,
            @RequestHeader("Authorization") String token) {
        User user = extractUserFromToken(token);

        try {
            Mural updatedMural = muralService.updateMural(id, muralDto, user);
            return ResponseEntity.ok(updatedMural);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retorna 404 se não encontrar o mural
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Retorna 403 se não tiver permissão
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMural(@PathVariable UUID id, @RequestHeader("Authorization") String token) {
        User user = extractUserFromToken(token);

        try {
            muralService.deleteMural(id, user);
            return ResponseEntity.noContent().build(); // Retorna 204 ao excluir com sucesso
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Retorna 404 se não encontrar o mural
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Retorna 403 se não tiver permissão
        }
    }
}

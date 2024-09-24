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

import br.com.verbi.verbi.dto.CommentDto;
import br.com.verbi.verbi.entity.Comment;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.CommentService;
import br.com.verbi.verbi.service.MuralService;
import br.com.verbi.verbi.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/mural/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private MuralService muralService;

    @Autowired
    private UserService userService;

    @Autowired
    private JWTGenerator jwtGenerator; 

    @PostMapping("/write/{muralId}")
    public ResponseEntity<Comment> createComment(@PathVariable UUID muralId, 
                                                 @RequestBody CommentDto commentDto,
                                                 @RequestHeader("Authorization") String token) {
        // Extrair o token JWT e o email do usuário
        String actualToken = token.substring(7); // Remove o prefixo "Bearer "
        String email = jwtGenerator.getUsername(actualToken); // Extrair o email do token

        // Procurar o usuário no banco de dados
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Procurar o mural onde o comentário será postado
        Mural mural = muralService.findMuralById(muralId)
                .orElseThrow(() -> new NoSuchElementException("Mural não encontrado"));

        // Criar o comentário associado ao mural e ao usuário
        Comment comment = commentService.createComment(commentDto.getContent(), user, mural);

        // Retornar a resposta com o comentário criado
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/user/{name}")
    public Page<Comment> getCommentsByUserName(@PathVariable String name, Pageable pageable) {
        return commentService.findCommentByUserName(name, pageable);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable UUID id, 
                                             @RequestBody CommentDto commentDto, 
                                             @RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.substring(7); // Remove o "Bearer " do token
            String email = jwtGenerator.getUsername(actualToken); // Extrai o email do token
    
            User user = userService.findUserByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    
            Comment updatedComment = commentService.updateComment(id, commentDto, user);
            return ResponseEntity.ok(updatedComment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id, @RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7); // Remover o prefixo "Bearer "
        String email = jwtGenerator.getUsername(actualToken); // Obter o email do token
    
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
    
        commentService.deleteComment(id, user);
    
        return ResponseEntity.noContent().build();
    }
    
}

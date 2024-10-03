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
import org.springframework.web.server.ResponseStatusException;

import br.com.verbi.verbi.dto.CommentDto;
import br.com.verbi.verbi.dto.CommentResponseDto;
import br.com.verbi.verbi.entity.Comment;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.CommentService;
import br.com.verbi.verbi.service.MuralService;
import br.com.verbi.verbi.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Creates a new comment for a specific mural.
     *
     * @param muralId   The ID of the mural where the comment will be posted.
     * @param commentDto The DTO containing the comment's content.
     * @param token      The JWT token for user authentication.
     * @return ResponseEntity containing the created CommentResponseDto.
     */
    @PostMapping("/write/{muralId}")
    public ResponseEntity<CommentResponseDto> createComment(@PathVariable UUID muralId, 
                                                             @RequestBody CommentDto commentDto,
                                                             @RequestHeader("Authorization") String token) {
        // Extract JWT token and user email
        String actualToken = token.substring(7); // Remove "Bearer " prefix
        String email = jwtGenerator.getUsername(actualToken); // Extract email from token

        // Find user in the database
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Find the mural where the comment will be posted
        Mural mural = muralService.findMuralById(muralId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mural not found"));

        // Create the comment associated with the mural and user
        Comment comment = commentService.createComment(commentDto.getContent(), user, mural);

        // Create a CommentResponseDto to return
        CommentResponseDto responseDto = new CommentResponseDto(
            comment.getId(),
            comment.getContent(),
            user.getName(), // Assuming User has a getName() method
            comment.getCreatedAt() // Assuming the createdAt field is set in the Comment entity
        );

        // Return the response with the created comment DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Retrieves comments made by a specific user.
     *
     * @param name     The username of the user whose comments to retrieve.
     * @param pageable The pagination information.
     * @return A Page of comments made by the user.
     */
    @GetMapping("/user/{name}")
    public Page<Comment> getCommentsByUserName(@PathVariable String name, Pageable pageable) {
        return commentService.findCommentByUserName(name, pageable);
    }

    /**
     * Updates an existing comment.
     *
     * @param id        The ID of the comment to update.
     * @param commentDto The DTO containing the new content for the comment.
     * @param token      The JWT token for user authentication.
     * @return ResponseEntity containing the updated Comment.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable UUID id, 
                                                 @RequestBody CommentDto commentDto, 
                                                 @RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7); // Remove "Bearer " prefix
        String email = jwtGenerator.getUsername(actualToken); // Extract email from token

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Comment updatedComment = commentService.updateComment(id, commentDto, user);
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * Deletes a comment by its ID.
     *
     * @param id   The ID of the comment to delete.
     * @param token The JWT token for user authentication.
     * @return ResponseEntity with no content.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID id, @RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7); // Remove "Bearer " prefix
        String email = jwtGenerator.getUsername(actualToken); // Extract email from token

        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        commentService.deleteComment(id, user);

        return ResponseEntity.noContent().build();
    }
}

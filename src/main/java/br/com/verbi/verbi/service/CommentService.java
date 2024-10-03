package br.com.verbi.verbi.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.verbi.verbi.dto.CommentDto;
import br.com.verbi.verbi.entity.Comment;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.repository.CommentRepository;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Creates a new comment and associates it with the given user and mural.
     * 
     * @param content The content of the comment.
     * @param user    The user making the comment.
     * @param mural   The mural the comment is associated with.
     * @return The created Comment object.
     */
    @Transactional
    public Comment createComment(String content, User user, Mural mural) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user); // Associate the user with the comment
        comment.setMural(mural); // Associate the mural with the comment

        return commentRepository.save(comment); // Save and return the created comment
    }

    /**
     * Retrieves a comment by its ID.
     * 
     * @param id The ID of the comment to retrieve.
     * @return An Optional containing the Comment if found, or empty if not.
     */
    public Optional<Comment> findById(UUID id) {
        return commentRepository.findById(id);
    }

    /**
     * Finds comments made by a user based on their username.
     * 
     * @param name     The username of the user whose comments to find.
     * @param pageable The pagination information.
     * @return A Page of comments made by the user.
     */
    public Page<Comment> findCommentByUserName(String name, Pageable pageable) {
        return commentRepository.findCommentsByUserName(name, pageable);
    }

    /**
     * Updates an existing comment.
     * 
     * @param id         The ID of the comment to update.
     * @param commentDto The DTO containing the new content for the comment.
     * @param user       The user attempting to update the comment.
     * @return The updated Comment object.
     * @throws ResponseStatusException if the comment is not found or if the user
     *                                 does not have permission.
     */
    @Transactional
    public Comment updateComment(UUID id, CommentDto commentDto, User user) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment Not Found"));

        // Check if the user has permission to update the comment
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to update this comment.");
        }

        comment.setContent(commentDto.getContent()); // Update the content of the comment
        return commentRepository.save(comment); // Save and return the updated comment
    }

    /**
     * Deletes a comment by its ID.
     * 
     * @param id   The ID of the comment to delete.
     * @param user The user attempting to delete the comment.
     * @throws ResponseStatusException if the comment is not found or if the user
     *                                 does not have permission.
     */
    @Transactional
    public void deleteComment(UUID id, User user) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment Not Found"));

        // Check if the user has permission to delete the comment
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to delete this comment.");
        }

        commentRepository.delete(comment); // Delete the comment
    }
}

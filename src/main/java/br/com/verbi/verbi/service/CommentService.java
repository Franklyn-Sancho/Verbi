package br.com.verbi.verbi.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.dto.CommentDto;
import br.com.verbi.verbi.entity.Comment;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.repository.CommentRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment createComment(String content, User user, Mural mural) {

        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setContent(content);
        comment.setUser(user);
        comment.setMural(mural);

        return commentRepository.save(comment);

    }

    public Optional<Comment> findById(UUID id) {
        return commentRepository.findById(id);
    }

    public Page<Comment> findCommentByUserName(String name, Pageable pageable) {
        return commentRepository.findCommentsByUserName(name, pageable);
    }

    public Comment updateComment(UUID id, CommentDto commentDto, User user) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment Not Found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to update this comment.");
        }

        comment.setContent(commentDto.getContent());
        return commentRepository.save(comment);
    }

    public void deleteComment(UUID id, User user) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment Not Found"));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this comment.");
        }

        commentRepository.delete(comment);
    }

}

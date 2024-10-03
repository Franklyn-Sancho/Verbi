package br.com.verbi.verbi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.verbi.verbi.entity.Comment;

import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Method to find a comment by its ID
    Optional<Comment> findById(UUID id);

    // Custom query to find comments by the user's name with pagination
    @Query("SELECT c FROM Comment c WHERE c.user.name = :name")
    Page<Comment> findCommentsByUserName(@Param("name") String name, Pageable pageable);
}

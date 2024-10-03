package br.com.verbi.verbi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.enums.MuralVisibility;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

// Repository interface for Mural entity
@Repository
public interface MuralRepository extends JpaRepository<Mural, UUID> {

    // Find a mural by its ID
    Optional<Mural> findById(UUID Id);

    // Find murals by user name with pagination support
    @Query("SELECT m FROM Mural m WHERE m.user.name = :name")
    Page<Mural> findMuralsByUserName(@Param("name") String name, Pageable pageable);

    // Method to find murals by visibility
    List<Mural> findByVisibility(MuralVisibility visibility);

    // Method to find murals of friends with visibility restricted to friends
    List<Mural> findByUserInAndVisibility(List<User> users, MuralVisibility visibility);
}

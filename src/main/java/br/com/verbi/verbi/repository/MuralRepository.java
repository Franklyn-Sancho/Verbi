package br.com.verbi.verbi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.verbi.verbi.entity.Mural;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

@Repository
public interface MuralRepository extends JpaRepository<Mural, UUID> {

    Optional<Mural> findById(UUID Id);

    @Query("SELECT m FROM Mural m WHERE m.user.name = :name")
    Page<Mural> findMuralsByUserName(@Param("name") String name, Pageable pageable);
    
}

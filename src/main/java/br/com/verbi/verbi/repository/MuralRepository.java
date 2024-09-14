package br.com.verbi.verbi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.verbi.verbi.entity.Mural;

@Repository
public interface MuralRepository extends JpaRepository<Mural, Long> {

    Optional<Mural> findById(Long Id);


    
}

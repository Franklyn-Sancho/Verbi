package br.com.verbi.verbi.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import br.com.verbi.verbi.dto.MuralDto;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.repository.MuralRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
public class MuralService {

    @Autowired
    private MuralRepository muralRepository;

    public Mural createMural(String body, User user) {

        Mural mural = new Mural();
        mural.setId(UUID.randomUUID());
        mural.setBody(body);
        mural.setUser(user);

        return muralRepository.save(mural);

    }

    public Optional<Mural> findMuralById(UUID id) {
        return muralRepository.findById(id);
    }

    public Page<Mural> findMuralsByUserName(String name, Pageable pageable) {
        return muralRepository.findMuralsByUserName(name, pageable);
    }
    

    public Mural updateMural(UUID id, MuralDto muralDto, User user) {
        Mural mural = muralRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mural Not Found"));

        if(!mural.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to update this mural.");
        }

        mural.setBody(muralDto.getBody());
        return muralRepository.save(mural);
    }

    public void deleteMural(UUID id, User user) {
        Mural mural = muralRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mural Not Found"));
    
        if (!mural.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this mural.");
        }
    
        muralRepository.delete(mural);
    }

}

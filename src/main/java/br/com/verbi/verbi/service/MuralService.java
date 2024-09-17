package br.com.verbi.verbi.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.repository.MuralRepository;

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

    public Optional<Mural> findMuralById(Long id) {
        return muralRepository.findById(id);
    }
}

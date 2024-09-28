package br.com.verbi.verbi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import br.com.verbi.verbi.dto.MuralDto;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.enums.MuralVisibility;
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.repository.MuralRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class MuralService {

    @Autowired
    private MuralRepository muralRepository;

    public Mural createMural(String body, MuralVisibility visibility, User user) {
        if (user == null) {
            throw new UserNotFoundException("User not found"); // Verifique se o usuário é nulo
        }

        Mural mural = new Mural();
        mural.setBody(body);
        mural.setVisibility(visibility);
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
                .orElseThrow(() -> new EntityNotFoundException("Mural Not Found")); // Melhor exceção

        // Verificação de permissões
        if (!mural.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to update this mural.");
        }

        mural.setBody(muralDto.getBody());
        mural.setVisibility(muralDto.getVisibility()); // Atualiza a visibilidade
        return muralRepository.save(mural);
    }

    public void deleteMural(UUID id, User user) {
        Mural mural = muralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mural Not Found"));

        // Verificação de permissões
        if (!mural.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this mural.");
        }

        muralRepository.delete(mural);
    }

    // Método para listar murais de acordo com a visibilidade
    public List<Mural> getVisibleMurals(User user) {
        List<Mural> globalMurals = muralRepository.findByVisibility(MuralVisibility.GLOBAL);
        List<Mural> friendMurals = muralRepository.findByUserInAndVisibility(user.getFriends(),
                MuralVisibility.FRIENDS_ONLY);

        List<Mural> allMurals = new ArrayList<>();
        allMurals.addAll(globalMurals);
        allMurals.addAll(friendMurals);

        return allMurals;
    }
}

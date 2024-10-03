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
    private MuralRepository muralRepository; // Repository for mural entity

    /**
     * Creates a new mural.
     * 
     * @param body     The content of the mural.
     * @param visibility The visibility setting of the mural.
     * @param user     The user associated with the mural.
     * @return The created mural.
     * @throws UserNotFoundException if the user is null.
     */
    public Mural createMural(String body, MuralVisibility visibility, User user) {
        if (user == null) {
            throw new UserNotFoundException("User not found"); // Check if the user is null
        }

        Mural mural = new Mural();
        mural.setBody(body);
        mural.setVisibility(visibility);
        mural.setUser(user); // Set the user associated with the mural

        return muralRepository.save(mural); // Save and return the created mural
    }

    /**
     * Finds a mural by its ID.
     * 
     * @param id The ID of the mural to find.
     * @return An Optional containing the mural if found, or empty if not.
     */
    public Optional<Mural> findMuralById(UUID id) {
        return muralRepository.findById(id); // Return the mural if found
    }

    /**
     * Finds murals by user name with pagination.
     * 
     * @param name     The name of the user whose murals to find.
     * @param pageable The pagination information.
     * @return A paginated list of murals.
     */
    public Page<Mural> findMuralsByUserName(String name, Pageable pageable) {
        return muralRepository.findMuralsByUserName(name, pageable); // Return the paginated list of murals
    }

    /**
     * Updates an existing mural.
     * 
     * @param id       The ID of the mural to update.
     * @param muralDto The DTO containing updated mural data.
     * @param user     The user attempting to update the mural.
     * @return The updated mural.
     * @throws EntityNotFoundException if the mural is not found.
     * @throws AccessDeniedException    if the user does not have permission to update the mural.
     */
    public Mural updateMural(UUID id, MuralDto muralDto, User user) {
        Mural mural = muralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mural Not Found")); // Throw exception if mural not found

        // Permission check for updating the mural
        if (!mural.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to update this mural.");
        }

        mural.setBody(muralDto.getBody()); // Update mural content
        mural.setVisibility(muralDto.getVisibility()); // Update mural visibility
        return muralRepository.save(mural); // Save and return the updated mural
    }

    /**
     * Deletes a mural by its ID.
     * 
     * @param id   The ID of the mural to delete.
     * @param user The user attempting to delete the mural.
     * @throws EntityNotFoundException if the mural is not found.
     * @throws AccessDeniedException    if the user does not have permission to delete the mural.
     */
    public void deleteMural(UUID id, User user) {
        Mural mural = muralRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mural Not Found")); // Throw exception if mural not found

        // Permission check for deleting the mural
        if (!mural.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You don't have permission to delete this mural.");
        }

        muralRepository.delete(mural); // Delete the mural
    }

    /**
     * Lists visible murals based on visibility settings.
     * 
     * @param user The user requesting visible murals.
     * @return A list of visible murals.
     */
    public List<Mural> getVisibleMurals(User user) {
        List<Mural> globalMurals = muralRepository.findByVisibility(MuralVisibility.GLOBAL); // Find global murals
        List<Mural> friendMurals = muralRepository.findByUserInAndVisibility(user.getFriends(),
                MuralVisibility.FRIENDS_ONLY); // Find friend-only murals

        List<Mural> allMurals = new ArrayList<>();
        allMurals.addAll(globalMurals); // Add global murals to the list
        allMurals.addAll(friendMurals); // Add friend murals to the list

        return allMurals; // Return all visible murals
    }
}

package br.com.verbi.verbi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.verbi.verbi.dto.MuralDto;
import br.com.verbi.verbi.dto.MuralResponseDto;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.AccessDeniedException;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.MuralService;
import br.com.verbi.verbi.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mural")
public class MuralController {

    @Autowired
    private MuralService muralService; // Service for mural operations

    @Autowired
    private UserService userService; // Service for user operations

    @Autowired
    private JWTGenerator jwtGenerator; // JWT token generator

    /**
     * Extracts user information from the provided JWT token.
     * 
     * @param token The JWT token containing user information.
     * @return The user associated with the token.
     * @throws UsernameNotFoundException if the user is not found.
     */
    private User extractUserFromToken(String token) {
        String actualToken = token.substring(7); // Remove "Bearer " prefix
        String email = jwtGenerator.getUsername(actualToken); // Get username from token
        return userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")); // User not found
    }

    /**
     * Creates a new mural.
     * 
     * @param muralDto The DTO containing mural data.
     * @param token    The JWT token for authentication.
     * @return ResponseEntity containing the created mural.
     */
    @PostMapping("/write")
    public ResponseEntity<MuralResponseDto> createMural(@Valid @RequestBody MuralDto muralDto,
            @RequestHeader("Authorization") String token) {
        User user = extractUserFromToken(token); // Extract user from token
        Mural createdMural = muralService.createMural(muralDto.getBody(), muralDto.getVisibility(), user);

        MuralResponseDto responseDto = new MuralResponseDto(
                createdMural.getId(),
                createdMural.getBody(),
                createdMural.getVisibility(),
                user.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Retrieves murals by the specified user name with pagination.
     * 
     * @param name     The name of the user whose murals to retrieve.
     * @param pageable The pagination information.
     * @return ResponseEntity containing a page of murals.
     */
    @GetMapping("/user/{name}")
    public ResponseEntity<Page<MuralResponseDto>> getMuralsByUserName(@PathVariable String name,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Mural> murals = muralService.findMuralsByUserName(name, pageable);
        Page<MuralResponseDto> responseDtos = murals.map(mural -> new MuralResponseDto(
                mural.getId(),
                mural.getBody(),
                mural.getVisibility(),
                mural.getUser().getName()
        ));
        return ResponseEntity.ok(responseDtos); // Return page of murals
    }

    /**
     * Updates an existing mural.
     * 
     * @param id       The ID of the mural to update.
     * @param muralDto The DTO containing updated mural data.
     * @param token    The JWT token for authentication.
     * @return ResponseEntity containing the updated mural or error status.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<MuralResponseDto> updateMural(@PathVariable UUID id,
            @Valid @RequestBody MuralDto muralDto,
            @RequestHeader("Authorization") String token) {
        User user = extractUserFromToken(token); // Extract user from token

        try {
            Mural updatedMural = muralService.updateMural(id, muralDto, user);
            MuralResponseDto responseDto = new MuralResponseDto(
                    updatedMural.getId(),
                    updatedMural.getBody(),
                    updatedMural.getVisibility(),
                    updatedMural.getUser().getName()
            );
            return ResponseEntity.ok(responseDto); // Return updated mural
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if not found
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Return 403 if not permitted
        }
    }

    /**
     * Deletes a mural by its ID.
     * 
     * @param id    The ID of the mural to delete.
     * @param token The JWT token for authentication.
     * @return ResponseEntity with no content or error status.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMural(@PathVariable UUID id, @RequestHeader("Authorization") String token) {
        User user = extractUserFromToken(token); // Extract user from token

        try {
            muralService.deleteMural(id, user);
            return ResponseEntity.noContent().build(); // Return 204 No Content
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 if not found
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Return 403 if not permitted
        }
    }

    /**
     * Lists visible murals for the authenticated user.
     * 
     * @param token The JWT token for authentication.
     * @return ResponseEntity containing a list of visible murals.
     */
    @GetMapping("/visible")
    public ResponseEntity<List<MuralResponseDto>> getVisibleMurals(@RequestHeader("Authorization") String token) {
        User user = extractUserFromToken(token); // Extract user from token
        List<Mural> visibleMurals = muralService.getVisibleMurals(user);
        
        List<MuralResponseDto> responseDtos = visibleMurals.stream()
                .map(mural -> new MuralResponseDto(
                        mural.getId(),
                        mural.getBody(),
                        mural.getVisibility(),
                        mural.getUser().getName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos); // Return visible murals
    }
}


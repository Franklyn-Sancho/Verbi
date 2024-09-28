package br.com.verbi.verbi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.verbi.verbi.dto.MuralDto;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.enums.MuralVisibility;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.repository.MuralRepository;

public class MuralServiceTest {

    @InjectMocks
    private MuralService muralService;

    @Mock
    private MuralRepository muralRepository;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateMural_ValidInput() {
        String body = "This is a mural post";
        MuralVisibility visibility = MuralVisibility.GLOBAL; // Defina a visibilidade aqui
        User user = new User();
        user.setId(UUID.randomUUID());

        Mural mural = new Mural();
        mural.setBody(body);
        mural.setVisibility(visibility); // Defina a visibilidade no mural
        mural.setUser(user);

        when(muralRepository.save(any(Mural.class))).thenReturn(mural);

        Mural createdMural = muralService.createMural(body, visibility, user); // Passe a visibilidade aqui

        assertEquals(mural.getBody(), createdMural.getBody());
        assertEquals(mural.getVisibility(), createdMural.getVisibility()); // Verifique se a visibilidade está
                                                                               // correta
        verify(muralRepository).save(any(Mural.class));
    }

    @Test
    public void testCreateMural_UserNotFound() {
        String body = "Mural Body";
        MuralVisibility visibility = MuralVisibility.GLOBAL; // Defina a visibilidade aqui

        // Simule a condição em que o usuário não é encontrado
        when(userService.findUserByEmail(anyString())).thenReturn(Optional.empty());

        // Tente criar um mural com um usuário não encontrado e verifique se uma exceção
        // é lançada
        assertThrows(UserNotFoundException.class, () -> {
            muralService.createMural(body, visibility, null); // Você pode passar um valor de visibilidade padrão
        });
    }

    @Test
    public void testUpdateMural_ValidInput() {
        UUID muralId = UUID.randomUUID();
        String updatedBody = "Updated mural post";
        User user = new User();
        user.setId(UUID.randomUUID());

        Mural existingMural = new Mural();
        existingMural.setId(muralId);
        existingMural.setBody("Old mural post");
        existingMural.setUser(user);

        MuralDto muralDto = new MuralDto();
        muralDto.setBody(updatedBody);

        when(muralRepository.findById(muralId)).thenReturn(Optional.of(existingMural));
        when(muralRepository.save(any(Mural.class))).thenReturn(existingMural);

        Mural updatedMural = muralService.updateMural(muralId, muralDto, user);

        assertEquals(updatedBody, updatedMural.getBody());
        verify(muralRepository).save(existingMural);
    }

    @Test
    public void testUpdateMural_NotFound() {
        UUID muralId = UUID.randomUUID();
        User user = new User();

        // Simular que o mural não foi encontrado
        when(muralRepository.findById(muralId)).thenReturn(Optional.empty());

        // Verificar se a exceção "Mural Not Found" é lançada
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            muralService.updateMural(muralId, new MuralDto(), user);
        });

        // Verificar se a mensagem de exceção está correta
        assertEquals("Mural Not Found", exception.getMessage());
    }

    @Test
    public void testDeleteMural_ValidInput() {
        UUID muralId = UUID.randomUUID();
        UUID userId = UUID.randomUUID(); // Cria um ID de usuário

        User user = new User();
        user.setId(userId); // Define o ID do usuário

        Mural existingMural = new Mural();
        existingMural.setId(muralId);
        existingMural.setUser(user);

        // Simular a busca do mural no repositório
        when(muralRepository.findById(muralId)).thenReturn(Optional.of(existingMural));

        // Chama o método de deletar o mural
        muralService.deleteMural(muralId, user);

        // Verifica se o método delete foi chamado corretamente
        verify(muralRepository).delete(existingMural);
    }

    @Test
    public void testDeleteMural_NotFound() {
        UUID muralId = UUID.randomUUID();
        User user = new User();

        // Simula a situação em que o mural não é encontrado no repositório
        when(muralRepository.findById(muralId)).thenReturn(Optional.empty());

        // Verifica se a exceção "Mural Not Found" é lançada
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            muralService.deleteMural(muralId, user);
        });

        // Verifica se a mensagem da exceção está correta
        assertEquals("Mural Not Found", exception.getMessage());
    }

}

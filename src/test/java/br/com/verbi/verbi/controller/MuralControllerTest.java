package br.com.verbi.verbi.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.verbi.verbi.dto.MuralDto;
import br.com.verbi.verbi.entity.Mural;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.enums.MuralVisibility;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.MuralService;
import br.com.verbi.verbi.service.UserService;

@ExtendWith(MockitoExtension.class)
public class MuralControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private MuralController muralController;

    @Mock
    private MuralService muralService;

    @Mock
    private UserService userService;

    @Mock
    private JWTGenerator jwtGenerator;

    private User user;
    private Mural mural;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(muralController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setName("Test User");

        mural = new Mural();
        mural.setId(UUID.randomUUID());
        mural.setBody("This is a test mural");
        mural.setUser(user);
        mural.setVisibility(MuralVisibility.GLOBAL); // Define a visibilidade do mural
    }

    @Test
    void testCreateMural_Success() throws Exception {
        MuralDto muralDto = new MuralDto();
        muralDto.setBody("This is a test mural");
        muralDto.setVisibility(MuralVisibility.GLOBAL); // Defina a visibilidade

        String token = "Bearer mockedToken";

        when(jwtGenerator.getUsername(anyString())).thenReturn(user.getEmail());
        when(userService.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(muralService.createMural(muralDto.getBody(), muralDto.getVisibility(), user)).thenReturn(mural);

        mockMvc.perform(post("/api/mural/write")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content("{\"body\": \"This is a test mural\", \"visibility\": \"GLOBAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("This is a test mural"))
                .andExpect(jsonPath("$.visibility").value("GLOBAL")); // Verifique a visibilidade

        verify(muralService, times(1)).createMural(muralDto.getBody(), muralDto.getVisibility(), user);
    }

    @Test
    void testUpdateMural_Success() throws Exception {
        UUID muralId = mural.getId();
        MuralDto muralDto = new MuralDto();
        muralDto.setBody("Updated mural body");
        muralDto.setVisibility(MuralVisibility.FRIENDS_ONLY); // Defina a nova visibilidade

        String token = "Bearer mockedToken";

        when(jwtGenerator.getUsername(anyString())).thenReturn(user.getEmail());
        when(userService.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(muralService.updateMural(eq(muralId), any(MuralDto.class), eq(user))).thenAnswer(invocation -> {
            MuralDto dto = invocation.getArgument(1);
            mural.setBody(dto.getBody());
            mural.setVisibility(dto.getVisibility()); // Atualiza a visibilidade
            return mural;
        });

        mockMvc.perform(put("/api/mural/update/{id}", muralId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content("{\"body\": \"Updated mural body\", \"visibility\": \"FRIENDS_ONLY\"}")) // Inclui visibilidade
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Updated mural body"))
                .andExpect(jsonPath("$.visibility").value("FRIENDS_ONLY")); // Verifica se a visibilidade foi atualizada
                                                                            // corretamente

        verify(muralService, times(1)).updateMural(eq(muralId),
                argThat(dto -> dto.getBody().equals("Updated mural body")
                        && dto.getVisibility() == MuralVisibility.FRIENDS_ONLY),
                eq(user));
    }

    @Test
    void testDeleteMural_Success() throws Exception {
        UUID muralId = mural.getId();
        String token = "Bearer mockedToken";

        when(jwtGenerator.getUsername(anyString())).thenReturn(user.getEmail());
        when(userService.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/mural/delete/{id}", muralId)
                .header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(muralService, times(1)).deleteMural(muralId, user);
    }
}

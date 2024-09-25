package br.com.verbi.verbi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import br.com.verbi.verbi.dto.LoginDto;
import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.repository.UserRepository;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.TokenBlacklistService;
import br.com.verbi.verbi.service.UserService;

public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MultipartFile picture;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testRegisterUser_Success() {
        UserDto userDto = new UserDto();
        userDto.setName("test");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");
        userDto.setPicture(null);

        User user = new User();
        user.setName("test");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);

        when(userService.registerUserWithEmail(any(UserDto.class), any(MultipartFile.class)))
                .thenReturn(user);

        ResponseEntity<User> response = userController.registerUser(userDto, picture);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());

        verify(userService, times(1)).registerUserWithEmail(userDto, picture);
    }

    @Test
    public void testLogin_Success() throws Exception {
        LoginDto loginDto = new LoginDto("test@example.com", "password123");
        String token = "mockedToken";

        when(userService.findUserByEmail(loginDto.getEmail())).thenReturn(Optional.of(new User()));
        when(userService.authenticateUser(loginDto.getEmail(), loginDto.getPassword())).thenReturn(true);
        when(jwtGenerator.generateToken(loginDto.getEmail())).thenReturn(token);

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"password\": \"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("Bearer " + token));
    }

    @Test
    public void testLogin_Unauthorized() throws Exception {
        LoginDto loginDto = new LoginDto("test@example.com", "wrongpassword");

        when(userService.findUserByEmail(loginDto.getEmail())).thenReturn(Optional.of(new User()));
        when(userService.authenticateUser(loginDto.getEmail(), loginDto.getPassword())).thenReturn(false);

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@example.com\", \"password\": \"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    public void testLogout_Success() throws Exception {
        String token = "Bearer mockedToken";

        mockMvc.perform(post("/api/user/logout")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout Successfully"));

        verify(tokenBlacklistService, times(1)).blacklistToken("mockedToken");
    }

    @Test
    public void testGetUserById_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        when(userService.findUserById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.findUserById(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto();
        userDto.setName("New Name");
        userDto.setEmail("new@example.com");
        userDto.setDescription("New description");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(userDto.getName());
        updatedUser.setEmail(userDto.getEmail());

        when(userService.updateUser(eq(userId), any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/user/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Name\", \"email\":\"new@example.com\", \"description\":\"New description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    public void testUpdateUser_NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto();
        userDto.setName("New Name");
        userDto.setEmail("new@example.com");
        userDto.setName("New description");

        when(userService.updateUser(eq(userId), any(UserDto.class))).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Name\", \"email\":\"new@example.com\", \"description\":\"New description\"}"))
                .andExpect(status().isNotFound());
    }

}

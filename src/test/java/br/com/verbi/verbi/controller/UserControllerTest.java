package br.com.verbi.verbi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.time.LocalDateTime;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import br.com.verbi.verbi.dto.LoginDto;
import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.TokenExpiredException;
import br.com.verbi.verbi.exception.UserNotFoundException;
import br.com.verbi.verbi.repository.UserRepository;
import br.com.verbi.verbi.security.JWTGenerator;
import br.com.verbi.verbi.service.EmailService;
import br.com.verbi.verbi.service.TokenBlacklistService;
import br.com.verbi.verbi.service.UserService;

@ExtendWith(MockitoExtension.class)
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
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

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

        mockMvc.perform(put("/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Name\", \"email\":\"new@example.com\", \"description\":\"New description\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRequestPasswordReset_UserFound() throws Exception {
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setResetPasswordToken("valid_token");

        
        when(userService.requestPasswordReset(email)).thenReturn(user);
        
        doNothing().when(emailService).sendResetPasswordEmail(any(User.class), any(String.class));

        
        mockMvc.perform(post("/api/user/password/request-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset email sent."));

        
        verify(emailService).sendResetPasswordEmail(user, "http://localhost:8080/reset-password?token=valid_token");
    }

    @Test
    public void testRequestPasswordReset_UserNotFound() throws Exception {
        String email = "nonexistent@example.com";

        when(userService.requestPasswordReset(email))
                .thenThrow(new UserNotFoundException("No user found with this email."));

        mockMvc.perform(post("/api/user/password/request-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No user found with this email."));
    }

    @Test
    public void testResetPassword_ValidToken() throws Exception {
        String token = "valid_token";
        String newPassword = "newPassword123";

        doNothing().when(userService).resetPassword(token, newPassword);

        mockMvc.perform(post("/api/user/password/reset")
                .param("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"" + newPassword + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password has been reset successfully."));
    }

    @Test
    public void testResetPassword_ExpiredToken() throws Exception {
        String token = "expired_token";
        String newPassword = "newPassword";

        User user = new User();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpires(LocalDateTime.now().minusHours(1));

        
        doThrow(new TokenExpiredException("The reset password token has expired."))
                .when(userService).resetPassword(token, newPassword);

        
        mockMvc.perform(post("/api/user/password/reset")
                .param("token", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"" + newPassword + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The reset password token has expired."));
    }

}

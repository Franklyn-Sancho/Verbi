package br.com.verbi.verbi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.verbi.verbi.dto.UserDto;
import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorizationService authorizationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testRegisterUserWithEmail_Success() {

        UserDto userDto = new UserDto();
        userDto.setName("test");
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");
        userDto.setPicture(null);

        User user = new User();
        user.setName("test");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = userService.registerUserWithEmail(userDto, null);

        assertNotNull(registeredUser);
        assertEquals("test@example.com", registeredUser.getEmail());
        assertEquals("encodedPassword", registeredUser.getPassword());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserWithEmail_EmailAlreadyExists() {

        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {

            userService.registerUserWithEmail(userDto, null);
        });

        assertEquals("Email already in use", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticateUser_Failure() {
        String email = "test@example.com";
        String password = "password";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean authenticated = userService.authenticateUser(email, password);

        assertFalse(authenticated);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testAuthenticateUser_Success() {
        String email = "test@example.com";
        String password = "password123";
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedPassword"); // O hash real da senha

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        boolean result = userService.authenticateUser(email, password);

        assertTrue(result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testAuthenticateUser_Unauthorized() {
        String email = "test@example.com";
        String password = "wrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(passwordEncoder.matches(eq(password), anyString())).thenReturn(false);

        boolean result = userService.authenticateUser(email, password);

        assertFalse(result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testFindUserById_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
    }

    @Test
    public void testFindUserById_NotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserById(userId);

        assertFalse(result.isPresent());
    }

    @Test
    public void testUpdateUser_Success() {
        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto();
        userDto.setName("New Name");
        userDto.setEmail("new@example.com");
        userDto.setDescription("New description");

        User user = new User();
        user.setId(userId);
        user.setName("Old Name");
        user.setEmail("old@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateUser(userId, userDto);

        assertEquals("New Name", updatedUser.getName());
        assertEquals("new@example.com", updatedUser.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    public void testUpdateUser_NotFound() {
        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto();
        userDto.setName("New Name");
        userDto.setEmail("new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(userId, userDto);
        });

        assertEquals("User not found with id " + userId.toString(), exception.getMessage());
    }

}

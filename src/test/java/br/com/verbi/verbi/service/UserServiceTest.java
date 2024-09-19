package br.com.verbi.verbi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.repository.UserRepository;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testRegisterUserWithValidData() {
        // Setup de dados para registro
        String name = "Teste";
        String email = "teste@teste.com";
        String password = "123456";

        // Mock do comportamento do repositório para evitar conflitos de email
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Mock do comportamento de salvar um usuário
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);  // Aqui pode ser o password codificado
        Mockito.when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Chamar o método de registro
        User result = userService.registerUser(name, email, password);

        // Verificar se o retorno é o esperado
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }
}

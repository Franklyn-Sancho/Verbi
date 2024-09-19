package br.com.verbi.verbi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.verbi.verbi.repository.TokenBlacklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class TokenBlacklistFilterTest {

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private TokenBlacklistFilter tokenBlacklistFilter;

    @Test
    public void testTokenIsBlacklisted() throws Exception {
        // Simulando o comportamento do repositório
        when(tokenBlacklistRepository.existsByToken("blacklisted_token")).thenReturn(true);

        // Criando requisição e resposta simuladas
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer blacklisted_token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        // Executando o filtro
        tokenBlacklistFilter.doFilterInternal(request, response, filterChain);

        // Verificando se o código de resposta é 401 (Unauthorized)
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        // Verificando se o filtro não chamou o filterChain
        verify(filterChain, Mockito.times(0)).doFilter(request, response);
    }
}


package br.com.verbi.verbi.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;


public class MyChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        MessageHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, MessageHeaderAccessor.class);

        if (headerAccessor != null) {
            Object authTokenObject = headerAccessor.getHeader("Authorization");
            System.out.println("Authorization header: " + authTokenObject); // Log do cabeçalho

            if (authTokenObject instanceof String) {
                String authToken = (String) authTokenObject;
                // Lógica de validação do token
                if (!isValidToken(authToken)) {
                    throw new SecurityException("Invalid authentication token");
                }
            } else {
                throw new SecurityException("Authorization header is missing or not a valid string");
            }
        }
        return message; // Retorna a mensagem se a verificação passar
    }

    private boolean isValidToken(String token) {
        // Lógica para validar o token (ex: JWT)
        return token != null && !token.isEmpty();
    }
}

package br.com.verbi.verbi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.verbi.verbi.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailQueueService emailQueueService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendConfirmationEmail_Success() throws MessagingException {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setEmailConfirmationToken("token123");

        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class); // Use um mock
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage); // Simula o retorno

        emailService.sendConfirmationEmail(user);

        verify(mailSender, times(1)).send(mimeMessage); // Verifique se o mimeMessage é o que foi realmente enviado
    }

    @Test
    public void testSendConfirmationEmail_Fail_QueueEmail() throws MessagingException {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setEmailConfirmationToken("token123");

        MimeMessage mimeMessage = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage); // Simula o retorno

        doThrow(new MailException("Mail sending failed") {
        }).when(mailSender).send(mimeMessage);

        assertThrows(MailException.class, () -> {
            emailService.sendConfirmationEmail(user);
        });

        verify(emailQueueService, times(1)).sendEmailToQueue(
                eq(user.getEmail()),
                eq("Email Confirmation"), // Mudei para "Email Confirmation" para alinhar com o método
                anyString());
    }

    @Test
    public void testSendResetPasswordEmail_Success() throws Exception {
        // Configura um objeto User para simular os dados do usuário
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");

        String resetPasswordLink = "http://localhost:8080/reset-password?token=someToken";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendResetPasswordEmail(user, resetPasswordLink);

        verify(mailSender, times(1)).send(eq(mimeMessage));
    }

    @Test
    public void testSendResetPasswordEmail_Fail_QueueEmail() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");

        String resetPasswordLink = "http://localhost:8080/reset-password?token=someToken";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new MailException("Mail sending failed") {
        }).when(mailSender).send(any(MimeMessage.class));

        assertThrows(MailException.class, () -> {
            emailService.sendResetPasswordEmail(user, resetPasswordLink);
        });

        verify(emailQueueService, times(1)).sendEmailToQueue(
                eq(user.getEmail()),
                eq("Password Reset"),
                anyString());
    }

}

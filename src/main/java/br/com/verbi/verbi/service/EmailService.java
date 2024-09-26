package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.User;
import br.com.verbi.verbi.exception.EmailCreationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailQueueService emailQueueService; // Injeta o EmailQueueService

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

    public void sendConfirmationEmail(User user) {
        String confirmationLink = generateConfirmationLink(user);

        MimeMessage message;
        try {
            message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Email Confirmation");
            helper.setText("Hello " + user.getName()
                    + ",\n\nPlease click the following link to confirm your email:\n" + confirmationLink, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailCreationException("Failed to create confirmation email message", e);
        } catch (MailException e) {

            emailQueueService.sendEmailToQueue(user.getEmail(), "Email Confirmation", "Hello " + user.getName()
                    + ",\n\nPlease click the following link to confirm your email:\n" + confirmationLink);
            throw e;
        }
    }

    private String generateConfirmationLink(User user) {
        return "http://localhost:8080/confirm-email/" + user.getEmailConfirmationToken();
    }

    public void sendResetPasswordEmail(User user, String resetPasswordLink) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset");
            helper.setText("Click the following link to reset your password: " + resetPasswordLink, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to create reset password email message", e);
        } catch (MailException e) {

            emailQueueService.sendEmailToQueue(user.getEmail(), "Password Reset", resetPasswordLink);

            throw e;
        }
    }

}

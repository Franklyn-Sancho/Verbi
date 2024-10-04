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
    private EmailQueueService emailQueueService; // Injecting EmailQueueService

    /**
     * Sends an email using the provided parameters.
     *
     * @param to      recipient's email address
     * @param subject email subject
     * @param body    email body
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailCreationException("Failed to create email message", e);
        } catch (MailException e) {
            emailQueueService.sendEmailToQueue(to, subject, body); // Send to queue on failure
            throw new RuntimeException("Failed to send email, queued for later delivery.", e);
        }
    }

    /**
     * Sends a confirmation email to the user.
     *
     * @param user the user to whom the email is sent
     */
    public void sendConfirmationEmail(User user) {
        String confirmationLink = generateConfirmationLink(user);
        String body = String.format("Hello %s,\n\nPlease click the following link to confirm your email:\n%s",
                user.getName(), confirmationLink);
        sendEmail(user.getEmail(), "Email Confirmation", body);
    }

    /**
     * Generates a confirmation link for the user.
     *
     * @param user the user for whom the confirmation link is generated
     * @return the confirmation link as a string
     */
    private String generateConfirmationLink(User user) {
        return "http://localhost:8080/confirm-email/" + user.getEmailConfirmationToken();
    }

    /**
     * Sends a password reset email to the user.
     *
     * @param user            the user to whom the email is sent
     * @param resetPasswordLink the link for resetting the password
     */
    public void sendResetPasswordEmail(User user, String resetPasswordLink) {
        String body = "Click the following link to reset your password: " + resetPasswordLink;
        sendEmail(user.getEmail(), "Password Reset", body);
    }
}

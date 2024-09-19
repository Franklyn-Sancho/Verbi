package br.com.verbi.verbi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

    public void sendConfirmationEmail(User user, String confirmationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Confirmação de E-mail");
        message.setText("Olá " + user.getName() + ",\n\nPor favor, clique no link a seguir para confirmar seu e-mail:\n" + confirmationLink);

        mailSender.send(message);
    }

    public void sendResetPasswordEmail(User user, String resetPasswordLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Email to Reset Password: ");
        message.setText("Olá " + user.getName() + ",\n\nPlease, click on link to follow reset password:\n" + resetPasswordLink);

        mailSender.send(message);
    }
}

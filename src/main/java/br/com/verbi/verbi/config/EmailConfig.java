package br.com.verbi.verbi.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        // Atualize estas propriedades para refletir a configuração do MailHog
        props.put("mail.smtp.auth", "false"); // Desative a autenticação
        props.put("mail.smtp.starttls.enable", "false"); // Desative STARTTLS se não for necessário

        props.put("mail.debug", "true"); // Mantenha o debug ativado para ajudar na resolução de problemas

        return mailSender;
    }
}


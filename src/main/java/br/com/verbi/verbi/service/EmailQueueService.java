package br.com.verbi.verbi.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.verbi.verbi.config.RabbitMQConfig;
import br.com.verbi.verbi.entity.EmailMessage;

@Service
public class EmailQueueService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Sends an email message to the RabbitMQ queue.
     *
     * @param to      recipient's email address
     * @param subject email subject
     * @param body    email body
     */
    public void sendEmailToQueue(String to, String subject, String body) {
        EmailMessage emailMessage = new EmailMessage(to, subject, body);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, emailMessage);
    }

    /**
     * Processes email messages from the RabbitMQ queue.
     *
     * @param emailMessage the email message to be processed
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEmailQueue(EmailMessage emailMessage) {
        try {
            // Use the injected EmailService
            EmailService emailService = new EmailService();
            emailService.sendEmail(emailMessage.getTo(), emailMessage.getSubject(), emailMessage.getBody());
            System.out.println("Email sent successfully");
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
            // Optionally, handle retries or logging here
        }
    }
}

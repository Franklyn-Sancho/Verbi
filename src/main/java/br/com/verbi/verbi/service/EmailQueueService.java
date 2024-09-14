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
    
    @Autowired
    private EmailService emailService;

    public void sendEmailToQueue(String to, String subject, String body) {
        
        EmailMessage emailMessage = new EmailMessage(to, subject, body);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, emailMessage);
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEmailQueue(EmailMessage emailMessage) {

        try {
            emailService.sendEmail(emailMessage.getTo(), emailMessage.getSubject(), emailMessage.getBody());
            System.out.println("Email enviado com sucesso");
        } catch (Exception e) {
            System.out.println("Falha ao enviar e-mail" + e.getMessage());
        }
    }

}

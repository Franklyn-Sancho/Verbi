package br.com.verbi.verbi.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_QUEUE = "email-queue-verbi";

    /**
     * Creates a durable RabbitMQ queue for emails.
     *
     * @return a Queue instance
     */
    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true); // true = durable queue
    }
}

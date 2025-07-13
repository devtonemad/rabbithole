package de.gnubis.rabbithole;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitMQSender {

    private RabbitTemplate rabbitTemplate;

    private final RabbitMQConnectionService connectionService;

    @Autowired
    public RabbitMQSender(RabbitMQConnectionService connectionService) {
        this.connectionService = connectionService;
        // Initialize RabbitTemplate if connection already established
        if (connectionService.isConnected()) {
            this.rabbitTemplate = new RabbitTemplate(connectionService.createSpringConnectionFactory());
        }
    }

    /**
     * Refresh RabbitTemplate with new connection factory after connection parameters change.
     * Should be called after a successful connect.
     */
    public void refreshRabbitTemplate() {
        if (connectionService.isConnected()) {
            CachingConnectionFactory factory = connectionService.createSpringConnectionFactory();
            this.rabbitTemplate = new RabbitTemplate(factory);
        } else {
            this.rabbitTemplate = null;
        }
    }

    public void sendToQueue(String queueName, String message) {
        if (rabbitTemplate == null) {
            throw new IllegalStateException("RabbitTemplate is not initialized. Connect first.");
        }
        rabbitTemplate.convertAndSend(queueName, message);
    }

    public void sendToExchange(String exchange, String routingKey, String message, Map<String, Object> headers) {
        if (rabbitTemplate == null) {
            throw new IllegalStateException("RabbitTemplate is not initialized. Connect first.");
        }
        MessageProperties messageProperties = new MessageProperties();
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(messageProperties::setHeader);
        }
        rabbitTemplate.send(exchange, routingKey, new Message(message.getBytes(), messageProperties));
    }

    public void sendToStream(String stream, String message) {
        if (rabbitTemplate == null) {
            throw new IllegalStateException("RabbitTemplate is not initialized. Connect first.");
        }
        rabbitTemplate.convertAndSend(stream, message);
    }
}
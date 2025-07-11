package de.gnubis.rabbithole;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendToQueue(String queueName, String message) {
        rabbitTemplate.convertAndSend(queueName, message);
    }

    public void sendToExchange(String exchange, String routingKey, String message, Map<String, Object> headers) {
        MessageProperties messageProperties = new MessageProperties();
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(messageProperties::setHeader);
        }
        rabbitTemplate.send(exchange, routingKey, new Message(message.getBytes(), messageProperties));
    }

    public void sendToStream(String stream, String message) {
        rabbitTemplate.convertAndSend(stream, message);
    }
}
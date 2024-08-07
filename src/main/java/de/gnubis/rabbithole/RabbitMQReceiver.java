package de.gnubis.rabbithole;

import com.rabbitmq.stream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RabbitMQReceiver {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Environment environment;

    public RabbitMQReceiver() {
        // Create the environment to interact with the stream
        this.environment = Environment.builder()
                .host("localhost")
                .username("guest")
                .password("guest")
                .build();
    }

    public List<String> receiveMessages(String sourceType, String source) {
        List<String> messages = new ArrayList<>();
        if ("queue".equals(sourceType)) {
            org.springframework.amqp.core.Message message;
            while ((message = rabbitTemplate.receive(source)) != null) {
                messages.add(new String(message.getBody()));
            }
        } else if ("stream".equals(sourceType)) {
            // Implement stream reading logic here
            Consumer consumer = environment.consumerBuilder()
                    .stream(source)
                    .messageHandler((context, message) -> {
                        String content = new String(message.getBodyAsBinary());
                        synchronized (messages) {
                            messages.add(content);
                        }
                    })
                    .build();

            try {
                // Wait a bit to collect messages
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            consumer.close();
        }
        return messages;
    }
}
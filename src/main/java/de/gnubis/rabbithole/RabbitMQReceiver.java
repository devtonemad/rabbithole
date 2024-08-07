package de.gnubis.rabbithole;

import com.rabbitmq.stream.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RabbitMQReceiver {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.stream.port}")
    private int port;

    private Environment environment;

    public RabbitMQReceiver() {
        // The environment will be initialized in the init method
        this.environment = null;
    }

    @PostConstruct
    public void init() {
        // Create the environment to interact with the stream
        this.environment = Environment.builder()
                .host(host)
                .username(username)
                .password(password)
                .port(port)
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
                    .offset(OffsetSpecification.first())
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
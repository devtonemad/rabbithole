package de.gnubis.rabbithole;

import com.rabbitmq.stream.*;
import jakarta.annotation.PostConstruct;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RabbitMQReceiver {

    private RabbitTemplate rabbitTemplate;

    private Environment environment;

    private final RabbitMQConnectionService connectionService;

    public RabbitMQReceiver(RabbitMQConnectionService connectionService) {
        this.connectionService = connectionService;
        this.environment = null;
        this.rabbitTemplate = null;
        // Initialize only if connection is already established
        if (connectionService.isConnected()) {
            refreshResources();
        }
    }

    /**
     * Refresh or initialize RabbitTemplate and Environment based on current connection.
     * Should be called after connection is established or changes.
     */
    public void refreshResources() {
        if (connectionService.isConnected()) {
            // Refresh RabbitTemplate for queue consumption
            this.rabbitTemplate = new RabbitTemplate(connectionService.createSpringConnectionFactory());

            // Create Environment for streams
            this.environment = Environment.builder()
                    .host(connectionService.getHost())
                    .port(connectionService.getStreamPort())
                    .username(connectionService.getUsername())
                    .password(connectionService.getPassword())
                    .build();
        } else {
            this.rabbitTemplate = null;
            this.environment = null;
        }
    }

    public List<String> receiveMessages(String sourceType, String source, String startOffset) {
        List<String> messages = new ArrayList<>();
        if ("queue".equals(sourceType)) {
            if (rabbitTemplate == null) {
                throw new IllegalStateException("RabbitTemplate is not initialized. Connect first.");
            }
            org.springframework.amqp.core.Message message;
            while ((message = rabbitTemplate.receive(source)) != null) {
                messages.add(new String(message.getBody()));
            }
        } else if ("stream".equals(sourceType)) {
            if (environment == null) {
                throw new IllegalStateException("Stream environment not initialized. Connect first.");
            }
            OffsetSpecification offsetSpec;
            if (startOffset != null && !startOffset.isBlank()) {
                try {
                    long offsetLong = Long.parseLong(startOffset);
                    offsetSpec = OffsetSpecification.offset(offsetLong);
                } catch (NumberFormatException e) {
                    offsetSpec = OffsetSpecification.first();
                }
            } else {
                offsetSpec = OffsetSpecification.first();
            }

            Consumer consumer = environment.consumerBuilder()
                    .stream(source)
                    .offset(offsetSpec)
                    .messageHandler((context, message) -> {
                        String content = "[Offset: " + context.offset() + "] " + new String(message.getBodyAsBinary());
                        synchronized (messages) {
                            messages.add(content);
                        }
                    })
                    .build();

            try {
                // Wait some time to collect messages
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            consumer.close();
        }
        Collections.reverse(messages);
        return messages;
    }

    // Optionally, provide getters for testing or monitoring
    public boolean isInitialized() {
        return rabbitTemplate != null && environment != null;
    }
}
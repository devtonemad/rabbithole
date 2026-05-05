package de.gnubis.rabbithole;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.Producer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class RabbitMQSender {

    private RabbitTemplate rabbitTemplate;
    private Environment streamEnvironment;

    private final RabbitMQConnectionService connectionService;

    @Autowired
    public RabbitMQSender(RabbitMQConnectionService connectionService) {
        this.connectionService = connectionService;
        if (connectionService.isConnected()) {
            this.rabbitTemplate = new RabbitTemplate(connectionService.createSpringConnectionFactory());
            this.streamEnvironment = buildStreamEnvironment();
        }
    }

    public void refreshRabbitTemplate() {
        if (connectionService.isConnected()) {
            CachingConnectionFactory factory = connectionService.createSpringConnectionFactory();
            this.rabbitTemplate = new RabbitTemplate(factory);
            this.streamEnvironment = buildStreamEnvironment();
        } else {
            this.rabbitTemplate = null;
            this.streamEnvironment = null;
        }
    }

    private Environment buildStreamEnvironment() {
        return Environment.builder()
                .host(connectionService.getHost())
                .port(connectionService.getStreamPort())
                .username(connectionService.getUsername())
                .password(connectionService.getPassword())
                .build();
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

    public void sendToStream(String stream, String message, Map<String, Object> headers) {
        if (streamEnvironment == null) {
            throw new IllegalStateException("Stream environment not initialized. Connect first.");
        }
        Producer producer = streamEnvironment.producerBuilder()
                .stream(stream)
                .build();
        try {
            com.rabbitmq.stream.MessageBuilder builder = producer.messageBuilder();
            builder.properties().contentType("application/json");
            if (headers != null && !headers.isEmpty()) {
                headers.forEach((k, v) -> builder.applicationProperties().entry(k, v.toString()));
            }
            builder.addData(message.getBytes());
            CountDownLatch latch = new CountDownLatch(1);
            boolean[] confirmed = {false};
            producer.send(builder.build(), confirmationStatus -> {
                confirmed[0] = confirmationStatus.isConfirmed();
                latch.countDown();
            });
            boolean acked = latch.await(5, TimeUnit.SECONDS);
            if (!acked) {
                throw new IllegalStateException("Stream send timed out — broker did not confirm within 5s");
            }
            if (!confirmed[0]) {
                throw new IllegalStateException("Stream send rejected by broker");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            producer.close();
        }
    }
}

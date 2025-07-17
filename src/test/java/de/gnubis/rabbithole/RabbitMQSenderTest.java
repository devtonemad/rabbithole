package de.gnubis.rabbithole;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.verify;

@SpringBootTest
public class RabbitMQSenderTest {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @MockBean
    private RabbitTemplate rabbitTemplate;

//    @Test
    void sendToQueue_shouldSendMessageToCorrectQueue() {
        // Given
        String queueName = "testQueue";
        String message = "testMessage";

        // When
        rabbitMQSender.sendToQueue(queueName, message);

        // Then
        verify(rabbitTemplate).convertAndSend(queueName, message);
    }
}
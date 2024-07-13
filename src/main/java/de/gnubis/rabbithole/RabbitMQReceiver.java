package de.gnubis.rabbithole;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RabbitMQReceiver {

    private final CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<>();

    @RabbitListener(queues = "test_stream")
    public void receive(String message) {
        messages.add(0, message); // Add message to the top
    }

    public CopyOnWriteArrayList<String> getMessages() {
        return messages;
    }
}
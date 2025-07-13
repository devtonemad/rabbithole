package de.gnubis.rabbithole;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
public class RabbitMQConnectionService {

    private Connection connection;

    /**
     * Attempts to establish a new connection to RabbitMQ broker with given parameters.
     * If a connection is already open, it will be closed and replaced.
     *
     * @param host     RabbitMQ host
     * @param port     RabbitMQ port
     * @param username username for RabbitMQ
     * @param password password for RabbitMQ
     * @throws IOException
     * @throws TimeoutException
     */
    public synchronized void connect(String host, int port, String username, String password) throws IOException, TimeoutException {
        disconnect(); // close existing connection if any

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        connection = factory.newConnection();
    }

    /**
     * Disconnects from the RabbitMQ broker if connected.
     */
    public synchronized void disconnect() {
        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
            } catch (IOException e) {
                // log or handle error closing connection
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Checks if currently connected
     * @return true if connection is open
     */
    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }
}
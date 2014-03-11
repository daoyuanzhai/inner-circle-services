package com.innercircle.services.messaging;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MessagingManager {
    private static final String EXCHANGE_NAME = "logs";

    private static volatile MessagingManager instance;
    private static ConnectionFactory factory;
    private static Connection connection;
    private static Channel channel;

    private MessagingManager() {}

    private static void init() throws IOException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
    }

    public static MessagingManager getInstance() throws IOException {
        if (null == instance) {
            synchronized(MessagingManager.class) {
                if (null == instance) {
                    instance = new MessagingManager();
                    init();
                }
            }
        }
        return instance;
    }

    public void sendLog(final String log) throws IOException {
    	connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        channel.basicPublish(EXCHANGE_NAME, "", null, log.getBytes());
        System.out.println(" [x] Sent '" + log + "'");

        channel.close();
        connection.close();
    }
}

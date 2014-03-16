package com.innercircle.services.messaging;

import java.io.IOException;

import com.innercircle.services.Constants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MessagingManager {

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    public MessagingManager() {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
    }

    public void sendMessage(final String uid, final String message) throws IOException {
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(Constants.EXCHANGE_NAME, "direct");

        channel.basicPublish(Constants.EXCHANGE_NAME, uid, null, message.getBytes());
        System.out.println(" [x] Sent '" + uid + "':'" + message + "'");

        channel.close();
        connection.close();
    }
}

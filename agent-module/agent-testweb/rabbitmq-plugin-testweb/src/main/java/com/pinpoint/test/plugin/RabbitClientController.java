package com.pinpoint.test.plugin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@RestController
public class RabbitClientController {

    private static final int GET_RETRY = 30;
    private static final long GET_RETRY_INTERVAL_MILLIS = 100L;

    private final Connection connection;
    private Channel consumerChannel;

    public RabbitClientController(Connection rabbitClientConnection) {
        this.connection = rabbitClientConnection;
    }

    @PostConstruct
    public void startConsumer() throws IOException {
        this.consumerChannel = connection.createChannel();
        this.consumerChannel.basicConsume(RabbitMQTestConstants.CLIENT_PUSH_QUEUE, true, new RabbitClientConsumer(consumerChannel));
    }

    @PreDestroy
    public void stopConsumer() throws IOException, TimeoutException {
        if (consumerChannel != null && consumerChannel.isOpen()) {
            consumerChannel.close();
        }
    }

    // basicPublish(String, String, BasicProperties, byte[])
    @GetMapping("/client/publish")
    public String publish() throws IOException, TimeoutException {
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish(RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.CLIENT_PUSH_QUEUE,
                    MessageProperties.TEXT_PLAIN, bytes("Hello, basicPublish(byte[])!"));
        }
        return "OK";
    }

    // basicPublish(String, String, boolean, boolean, BasicProperties, ByteBuffer, WriteListener), added in amqp-client 5.31.0
    @GetMapping("/client/publish-buffer")
    public String publishBuffer() throws IOException, TimeoutException {
        try (Channel channel = connection.createChannel()) {
            final ByteBuffer body = ByteBuffer.wrap(bytes("Hello, basicPublish(ByteBuffer)!"));
            channel.basicPublish(RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.CLIENT_PUSH_QUEUE, false, false,
                    MessageProperties.TEXT_PLAIN, body, (success, cause) -> {
                    });
        }
        return "OK";
    }

    // basicPublish, then basicGet
    @GetMapping("/client/publish-and-get")
    public String publishAndGet() throws IOException, TimeoutException, InterruptedException {
        try (Channel channel = connection.createChannel()) {
            channel.basicPublish(RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.CLIENT_PULL_QUEUE,
                    MessageProperties.TEXT_PLAIN, bytes("Hello, basicGet!"));
            for (int i = 0; i < GET_RETRY; i++) {
                final GetResponse response = channel.basicGet(RabbitMQTestConstants.CLIENT_PULL_QUEUE, true);
                if (response != null) {
                    return "Received: " + new String(response.getBody(), StandardCharsets.UTF_8) + ", headers=" + response.getProps().getHeaders();
                }
                Thread.sleep(GET_RETRY_INTERVAL_MILLIS);
            }
        }
        return "Received: null";
    }

    private static byte[] bytes(String message) {
        return message.getBytes(StandardCharsets.UTF_8);
    }
}

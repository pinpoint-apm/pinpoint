package com.pinpoint.test.plugin;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.nio.charset.StandardCharsets;

/**
 * Overrides handleDelivery without calling super, so the agent has to be told about it with
 * profiler.rabbitmq.client.consumer.classes (see the module pom).
 */
public class RabbitClientConsumer extends DefaultConsumer {

    public RabbitClientConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        System.out.println("Received by the client consumer: " + new String(body, StandardCharsets.UTF_8) + ", headers=" + properties.getHeaders());
    }
}

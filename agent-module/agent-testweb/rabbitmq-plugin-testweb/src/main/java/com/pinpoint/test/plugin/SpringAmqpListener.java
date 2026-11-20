package com.pinpoint.test.plugin;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SpringAmqpListener {

    // SimpleMessageListenerContainer (BlockingQueueConsumer)
    @RabbitListener(id = "spring-simple", queues = RabbitMQTestConstants.SPRING_SIMPLE_QUEUE)
    public void listenSimple(Message message) {
        System.out.println("Received by the simple container: " + describe(message));
    }

    // DirectMessageListenerContainer (SimpleConsumer)
    @RabbitListener(id = "spring-direct", queues = RabbitMQTestConstants.SPRING_DIRECT_QUEUE,
            containerFactory = "directRabbitListenerContainerFactory")
    public void listenDirect(Message message) {
        System.out.println("Received by the direct container: " + describe(message));
    }

    static String describe(Message message) {
        return new String(message.getBody(), StandardCharsets.UTF_8) + ", headers=" + message.getMessageProperties().getHeaders();
    }
}

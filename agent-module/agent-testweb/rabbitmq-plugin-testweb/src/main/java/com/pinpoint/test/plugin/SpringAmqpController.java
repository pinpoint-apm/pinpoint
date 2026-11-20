package com.pinpoint.test.plugin;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringAmqpController {

    private static final long RECEIVE_TIMEOUT_MILLIS = 3000L;

    private final RabbitTemplate rabbitTemplate;

    public SpringAmqpController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/")
    public String welcome() {
        return "/spring/send, /spring/send-direct, /spring/send-and-receive, "
                + "/client/publish, /client/publish-buffer, /client/publish-and-get";
    }

    @GetMapping("/spring/send")
    public String send() {
        rabbitTemplate.convertAndSend(RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.SPRING_SIMPLE_QUEUE, "Hello, simple container!");
        return "OK";
    }

    @GetMapping("/spring/send-direct")
    public String sendDirect() {
        rabbitTemplate.convertAndSend(RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.SPRING_DIRECT_QUEUE, "Hello, direct container!");
        return "OK";
    }

    @GetMapping("/spring/send-and-receive")
    public String sendAndReceive() {
        rabbitTemplate.convertAndSend(RabbitMQTestConstants.EXCHANGE, RabbitMQTestConstants.SPRING_RECEIVE_QUEUE, "Hello, RabbitTemplate.receive!");
        final Message received = rabbitTemplate.receive(RabbitMQTestConstants.SPRING_RECEIVE_QUEUE, RECEIVE_TIMEOUT_MILLIS);
        if (received == null) {
            return "Received: null";
        }
        return "Received: " + SpringAmqpListener.describe(received);
    }
}

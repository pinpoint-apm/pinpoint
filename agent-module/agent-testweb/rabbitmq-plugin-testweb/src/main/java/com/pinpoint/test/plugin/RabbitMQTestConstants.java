package com.pinpoint.test.plugin;

public class RabbitMQTestConstants {

    public static final String EXCHANGE = "rabbitmq-testweb-exchange";

    // spring-rabbit, consumed by a SimpleMessageListenerContainer
    public static final String SPRING_SIMPLE_QUEUE = "rabbitmq-testweb-spring-simple";

    // spring-rabbit, consumed by a DirectMessageListenerContainer
    public static final String SPRING_DIRECT_QUEUE = "rabbitmq-testweb-spring-direct";

    // spring-rabbit, pulled with RabbitTemplate.receive
    public static final String SPRING_RECEIVE_QUEUE = "rabbitmq-testweb-spring-receive";

    // amqp-client, consumed by a Consumer registered with basicConsume
    public static final String CLIENT_PUSH_QUEUE = "rabbitmq-testweb-client-push";

    // amqp-client, pulled with basicGet
    public static final String CLIENT_PULL_QUEUE = "rabbitmq-testweb-client-pull";
}

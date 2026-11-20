package com.pinpoint.test.plugin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Configuration
public class RabbitMQConfig {

    private static final String[] QUEUES = {
            RabbitMQTestConstants.SPRING_SIMPLE_QUEUE,
            RabbitMQTestConstants.SPRING_DIRECT_QUEUE,
            RabbitMQTestConstants.SPRING_RECEIVE_QUEUE,
            RabbitMQTestConstants.CLIENT_PUSH_QUEUE,
            RabbitMQTestConstants.CLIENT_PULL_QUEUE,
    };

    // Every queue is bound to one direct exchange with its own name as the routing key.
    @Bean
    public Declarables rabbitDeclarables() {
        final DirectExchange exchange = new DirectExchange(RabbitMQTestConstants.EXCHANGE, false, true);
        final List<Declarable> declarables = new ArrayList<>();
        declarables.add(exchange);
        for (String queueName : QUEUES) {
            final Queue queue = new Queue(queueName, false, false, true);
            final Binding binding = BindingBuilder.bind(queue).to(exchange).with(queueName);
            declarables.add(queue);
            declarables.add(binding);
        }
        return new Declarables(declarables);
    }

    @Bean
    public DirectRabbitListenerContainerFactory directRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        final DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    // A plain amqp-client connection, separate from the one spring-rabbit manages.
    @Bean(destroyMethod = "close")
    public Connection rabbitClientConnection(@Value("${spring.rabbitmq.host}") String host,
                                             @Value("${spring.rabbitmq.port}") int port,
                                             @Value("${spring.rabbitmq.username}") String username,
                                             @Value("${spring.rabbitmq.password}") String password) throws IOException, TimeoutException {
        final com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setAutomaticRecoveryEnabled(true);

        final Connection connection = connectionFactory.newConnection("rabbitmq-testweb-client");
        declare(connection);
        return connection;
    }

    // Same declarations as rabbitDeclarables(), so the client endpoints do not depend on the listener containers having started.
    private void declare(Connection connection) throws IOException, TimeoutException {
        try (Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(RabbitMQTestConstants.EXCHANGE, "direct", false, true, null);
            for (String queueName : QUEUES) {
                channel.queueDeclare(queueName, false, false, true, null);
                channel.queueBind(queueName, RabbitMQTestConstants.EXCHANGE, queueName);
            }
        }
    }
}

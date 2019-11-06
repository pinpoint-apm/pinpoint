### Pinpoint RabbitMQ Plugin

#### Tracing custom Consumers
RabbitMQ plugin traces `Consumer.handleDelivery(...)` implementations to record calls made when a *basic.deliver* is
received by a consumer. 

This is done automatically for consumers implemented in RabbitMQ Java Client (**DefaultConsumer**, **QueueingConsumer**)
or in Spring-rabbit (**BlockingQueueConsumer$InternalConsumer**, **RabbitTemplate$TemplateConsumer**).

However, if you implemented your own **Consumer** and registered to a channel like below, you must tell the agent what
these consumers are unless the consumer explicitly calls `super.handleDelivery(...)`.
```
Consumer customConsumer = new DefaultConsumer(channel) {
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        // code
    }
};
channel.basicConsume(queue, customConsumer); 
```
To trace such consumers, please configure the following option in *pinpoint.config* with the fully qualified class name
of the consumer.
```
profiler.rabbitmq.client.consumer.classes=
```

#### Excluding exchanges
If you would like to exclude certain exchanges from being traced, please configure the following option in
*pinpoint.config*.
```
profiler.rabbitmq.client.exchange.exclude=
```

#### Supported libraries
The plugin has been tested on the following libraries.
```
com.rabbitmq:amqp-client - 2.7.0+
org.springframework.amqp:spring-rabbit - 1.3.3+
```
*(Pinpoint 1.7.2 supports up to org.springframework.amqp:spring-rabbit 1.3.3 ~ 1.7.2, 2.0.1 and 2.0.2)*
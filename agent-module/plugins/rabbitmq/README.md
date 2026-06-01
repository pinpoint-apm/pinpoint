### RabbitMQ
* Since: Pinpoint 1.7.0
* See: https://www.rabbitmq.com/
* Range: com.rabbitmq/amqp-client [3.0.0, 5.x]
* Range: org.springframework.amqp/spring-rabbit [1.3.3, 2.4] (optional, instrumented when present)

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
com.rabbitmq:amqp-client - 3.0.0+ (including 5.x)
org.springframework.amqp:spring-rabbit - 1.3.3+ (optional, instrumented when present on user classpath)
```
*(Pinpoint 1.7.2 supports up to org.springframework.amqp:spring-rabbit 1.3.3 ~ 1.7.2, 2.0.1 and 2.0.2)*

The plugin compiles only against `com.rabbitmq:amqp-client` and does not require `spring-rabbit` at build time.
Spring AMQP classes are instrumented dynamically when detected on the user application's classpath.
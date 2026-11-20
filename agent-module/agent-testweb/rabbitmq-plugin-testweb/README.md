# RabbitMQ plugin testweb

Spring Boot 4 app that exercises the RabbitMQ plugin through both `spring-rabbit` and the plain `amqp-client` API.

- `com.rabbitmq:amqp-client` 5.38.0 (`rabbitmq.amqp-client.version` in the module pom, newer than the Spring Boot BOM)
- `org.springframework.amqp:spring-rabbit` 4.x, managed by the Spring Boot 4 BOM

## Prerequirements
- JDK 17 (Spring Boot 4).
- A RabbitMQ broker on `localhost:5672` with the default `guest` / `guest` account
  (`spring.rabbitmq.*` in `application.yml`):
```
$ docker run -d --rm --name rabbitmq-testweb -p 5672:5672 rabbitmq:3-management
```
The exchange and the queues are declared by the app on startup and are deleted automatically.

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw install -pl agent-module/agent-testweb/rabbitmq-plugin-testweb -am -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/rabbitmq-plugin-testweb spring-boot:start
```

### spring-rabbit
- `RabbitTemplate.convertAndSend`, consumed by a `SimpleMessageListenerContainer`: http://localhost:18080/spring/send
- `RabbitTemplate.convertAndSend`, consumed by a `DirectMessageListenerContainer`: http://localhost:18080/spring/send-direct
- `RabbitTemplate.convertAndSend`, then `RabbitTemplate.receive`: http://localhost:18080/spring/send-and-receive

### amqp-client
- `basicPublish` with a `byte[]` body, consumed by a `Consumer` registered with `basicConsume`: http://localhost:18080/client/publish
- `basicPublish` with a `ByteBuffer` body (amqp-client 5.31.0+), same consumer: http://localhost:18080/client/publish-buffer
- `basicPublish`, then `basicGet`: http://localhost:18080/client/publish-and-get

Every consumer prints the received message headers to the console, and the two pull endpoints return them in the
response, so the `Pinpoint-*` headers injected by the producer side can be checked without a collector.

The `ByteBuffer` overload of `basicPublish` is not instrumented when it is called directly: `/client/publish-buffer`
shows `headers=null` and no producer span.

`RabbitClientConsumer` overrides `handleDelivery` without calling `super`, so it is registered with
`profiler.rabbitmq.client.consumer.classes` in the module pom.

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/rabbitmq-plugin-testweb spring-boot:stop
$ docker stop rabbitmq-testweb
```

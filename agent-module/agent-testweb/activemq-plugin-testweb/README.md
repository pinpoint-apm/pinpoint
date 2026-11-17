## Prerequirements
- JDK 17 (Spring Boot 4).
- No external ActiveMQ is needed: each app starts its own broker and connects to it over tcp.
  To use an external broker instead, set `activemq.broker.embedded=false` and point the broker url at it.
- ActiveMQ 5.x speaks `javax.jms` and 6.x speaks `jakarta.jms`, so each line has its own app.

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw install -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-5-original,agent-module/agent-testweb/activemq-plugin-testweb/activemq-6-springboot -am -Dmaven.test.skip=true
```

## ActiveMQ 5.x with the JMS API
- ActiveMQ 5.19.x client and broker, `tcp://localhost:61616` (`activemq.broker.url`)

### Run
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-5-original spring-boot:start
```
- Send, consumed by a MessageListener: http://localhost:18080/activemq5/send
- Send with an AsyncCallback, consumed by a MessageListener: http://localhost:18080/activemq5/sendAsync
- Send, then MessageConsumer.receive: http://localhost:18080/activemq5/sendAndReceive

### Stop
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-5-original spring-boot:stop
```

## ActiveMQ 6.x with Spring Boot JMS
- ActiveMQ 6.x client and broker managed by Spring Boot, `tcp://localhost:61617` (`spring.activemq.broker-url`)

### Run
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-6-springboot spring-boot:start
```
- JmsTemplate send, consumed by the default `@JmsListener` container (MessageConsumer.receive): http://localhost:18080/activemq6/send
- JmsTemplate send, consumed by a simple `@JmsListener` container (MessageListener): http://localhost:18080/activemq6/sendToSimpleListener
- JmsTemplate send, then JmsTemplate.receive: http://localhost:18080/activemq6/sendAndReceive

### Stop
```
$ ./mvnw -pl agent-module/agent-testweb/activemq-plugin-testweb/activemq-6-springboot spring-boot:stop
```

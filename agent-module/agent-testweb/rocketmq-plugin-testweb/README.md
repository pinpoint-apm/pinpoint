
## Prerequirements

- RocketMQ NameServer and Broker Server are required.

## Install
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb install -Dmaven.test.skip=true
```

## Send Message with original
## Run
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-original spring-boot:start
```
- Send Sync: http://localhost:18080/original/send
- Send ASync: http://localhost:18080/original/sendAsync

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb spring-boot:stop
```

## Send Message with SpringBoot
## Run
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-springboot spring-boot:start
```
- Send Sync: http://localhost:18080/template/send
- Send ASync: http://localhost:18080/template/sendAsync
## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-springboot spring-boot:stop
```

## Send Message with SpringCloud Stream
## Run
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-springcloud-stream spring-boot:start
```
- Send: http://localhost:18080/stream/send
## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-springcloud-stream  spring-boot:stop
```


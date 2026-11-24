
## Prerequirements

- RocketMQ NameServer and Broker Server are required, with the NameServer on `localhost:9876` (`namesrvAddr` in
  `application.yml`). The broker registers its own address with the NameServer and the client connects to that
  address, so `brokerIP1` has to be reachable from the app:
```
$ docker run -d --rm --name rocketmq-testweb -p 9876:9876 -p 10911:10911 -p 10909:10909 \
    -e "JAVA_OPT_EXT=-Xms512m -Xmx512m -Xmn256m" apache/rocketmq:5.3.2 \
    bash -c "printf 'brokerIP1=127.0.0.1\nautoCreateTopicEnable=true\n' > /home/rocketmq/broker.conf && (sh mqnamesrv &) && sleep 10 && exec sh mqbroker -n localhost:9876 -c /home/rocketmq/broker.conf"
```

## Install
```
$ ./mvnw install -pl agent-module/agent -am -Dmaven.test.skip=true
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb install -am -Dmaven.test.skip=true
```

## Send Message with original
- `org.apache.rocketmq:rocketmq-client` 5.5.1 (`testweb.rocketmq-client.version` in the module pom)

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-original spring-boot:start
```
- Send Sync: http://localhost:18080/original/send
- Send ASync: http://localhost:18080/original/sendAsync

Both are consumed by a `DefaultMQPushConsumer` with a `MessageListenerConcurrently`.

On a broker that was just started, the topic and the consumer group are new and the first messages are not
delivered right away. If no `Received message` line shows up, call the endpoint again after about a minute: the
earlier messages arrive together with the new one. Later runs against the same broker deliver immediately.

## Run with another rocketmq-client version
Override `testweb.rocketmq-client.version` and rebuild the module in the same command:
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-original clean package spring-boot:start -Dmaven.test.skip=true -Dtestweb.rocketmq-client.version=4.9.8
```
- Range: 4.0.0-incubating to 5.5.1, the range of the plugin. The same broker can be used for all of them.
- To check the version in use, look for `rocketmq-client-<version>.jar` in the classpath the agent prints at startup.
- The plugin does not trace everything on the oldest clients, so [Verify](#verify) gives less there:

| rocketmq-client | `/original/send` | `/original/sendAsync` | consumer `Span` |
|---|---|---|---|
| 4.0.0-incubating | headers | `headers={}` | none |
| 4.1.0-incubating, 4.2.0 | headers | `headers={}` | for `/original/send` only |
| 4.3.0 and later | headers | headers | for both |

## Verify
The app has no automated test. Call an endpoint and check the console of the terminal that ran `spring-boot:start`;
both the app output and the agent log are written there. No collector is needed.

### 1. Trace headers reach the consumer (app output)
The listener prints the `Pinpoint-*` properties of each message it receives:
```
Received message: Hello world, headers={Pinpoint-Flags=0, Pinpoint-SpanID=3823004845550260006, Pinpoint-TraceID=...^1795418682107^1, Pinpoint-pAppName=PluginTest, Pinpoint-pAppType=1210, Pinpoint-pServiceName=DEFAULT, Pinpoint-pSpanID=-8226638332028813564}
```
`headers={}` means the producer side did not inject them.

### 2. Spans are recorded (agent log)
Start the app with the agent at DEBUG so that every flushed `Span` / `SpanChunk` is logged, and filter on `Flush`:
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-original spring-boot:start -Dpinpoint.agent.logging.level=DEBUG
```
One call of `/original/send` flushes two spans (shortened):
```
BufferedStorage -- Flush Span{... parentSpanId=-1, spanId=-8226638332028813564 ... SpanEvent{... serviceType=8400, endPoint='localhost:9876' ... nextSpanId=3823004845550260006, destinationId='localhost:9876' ...
BufferedStorage -- Flush Span{... parentSpanId=-8226638332028813564, spanId=3823004845550260006 ... remoteAddr='localhost:9876' ... acceptorHost='localhost:9876' ...
```
Check that
- the request span has a producer `SpanEvent` with `serviceType=8400` (ROCKETMQ_CLIENT) and the NameServer address in
  `endPoint` and `destinationId`;
- a consumer `Span` exists with the NameServer address in `remoteAddr` and `acceptorHost`;
- they are linked: the consumer `parentSpanId` / `spanId` equal the request `spanId` and the producer `nextSpanId`
  (the same values as the `Pinpoint-pSpanID` / `Pinpoint-SpanID` headers).

For `/original/sendAsync` the producer event is flushed in an asynchronous `SpanChunk` of the request span, followed
by a second one for the `SendCallback` (`serviceType=8401`, ROCKETMQ_CLIENT_INTERNAL).

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/rocketmq-plugin-testweb/rocketmq-original spring-boot:stop
$ docker stop rocketmq-testweb
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


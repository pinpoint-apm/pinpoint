
## Purpose

This module abstracts the channel concept which are usually implemented by Redis, or Kafka, etc.

Regardless of the underlying implementation,
the channel is a way to send messages from one part of the system to another.
Also, the channel can provide the way to broadcast certain events to the unknown multiple subscribers.

### Usage

The channel should be obtained from channel repository, which provides the channel by URI.

Only the single channel repository should be exist in JVM,
and created with some pairs of `ChannelProvider` and its name.

```java
ChannelRepository repository = new ChannelRepository(List.of(
    ChannelProviderRegistry.of("redis", new RedisChannelProvider("redis")),
    ChannelProviderRegistry.of("kafka", new KafkaChannelProvider("kafka"))
))
```

Then, the channel can be obtained by the URI.

**Hello World Example**

At instance-subscriber, the process should print the message "Hello, world!" which is published by instance-publisher.

```java
// Instance-subscriber

URI uri = URI.create("redis://system-out?param=foo");
SubChannel subChannel = repository.getSubChannel(uri);
subChannel.subscribe(message -> {
    System.out.println(new String(message));
});
```

```java
// Instance-publisher

URI uri = URI.create("redis://system-out?param=foo");
PubChannel pubChannel = repository.getPubChannel(uri);
pubChannel.publish("Hello, world!".getBytes());
```

### Channel Service

This module also contains the ChannelService implementations which are used to manage instant
demand-supply interactions between the different parts of the system. These are very similar to the conventional
RPC calls, but it is designed to send demand to all the servers which are listening to the service.

There should be ChannelServiceServers, which supplies the demands in network in prior to the ChannelServiceClient
emitting the demand. Each servers catch all demands from the reserved channel for the service,
and exactly 0 or 1 server should supply the data to the supply channel.

For communication, all servers and clients must have the `ChannelServiceProtocol` which have the information
about the service, and the demand and supply channels.

```java
ChannelServiceProtocol protocol = ChannelServiceProtocol.<String, Long>builder()
    .setDemandSerde(JacksonSerde.byClass(objectMapper, String.class))
    .setDemandPubChannelURIProvider(demand -> URI.create("redis:char-count:demand"))
    .setDemandSubChannelURI(URI.create("redis:char-count:demand"))
    .setSupplySerde(JacksonSerde.byClass(objectMapper, Long.class))
    .setSupplyChannelURIProvider(demand -> URI.create("redis:char-count:supply:" + demand.hashCode()))
    .setRequestTimeout(Duration.ofSeconds(3))
    .buildMono();
```

With the protocol and the channel repository, the ChannelServiceClient and ChannelServiceServer can be created.

**Server**

```java
ChannelServiceServer.buildMono(repository, protocol, demand -> demand.length()).listen();
```

**Client**

```java
MonoChannelServiceClient client = ChannelServiceClient.buildMono(repository, protocol);
Long result = client.demand("Hello, World!").block(); // 13
```

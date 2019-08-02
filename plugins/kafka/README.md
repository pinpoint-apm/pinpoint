## Pinpoint Kafka Client Plugin (beta)

### Support Version 
0.11 ~  
1.x ~  
2.x ~
<br><br>

### Kafka Configuration
To enable Kafka Producer, set the following option in *pinpoint.config*:
```
profiler.kafka.producer.enable=true
```

To enable Kafka Consumer, set the following option in *pinpoint.config*:

#### to use consumer with spring-kafka
```
# Setting when using spring-kafka (In this case, you can leave profiler.kafka.consumer.entryPoint option to empty.)
profiler.springkafka.consumer.enable=true
```

#### to use consumer  exclusively
```
profiler.kafka.consumer.enable=true
# you must set target that handles ConsumerRecord or ConsumerRecords(Remote Trace feature is not enabled.) as a argument for remote trace
# ex) profiler.kafka.consumer.entryPoint=clazzName.methodName
profiler.kafka.consumer.entryPoint=
```
<br><br>


### Caution 
#### Caution for Consumer 
In the case of a Consumer, 
there is a difference between setting up to handle a single message(ConsumerRecord) and multi message(ConsumerRecords), as shown in the server map. 

If ConsumerRecord, which receives one message at a time, is the target of the trace, remote trace is enabled.   
  ![Kafka Server Map](../../doc/images/plugin/kafka/servermap1.png)  
(You can show the information of the producer who sent the message to the broker.)
<br><br>
If ConsumerRecords, which has received multiple messages at once, is the target, remote trace is disabled. 
  ![Kafka Server Map2](../../doc/images/plugin/kafka/servermap2.png)  
(You can not show the information of the producer who sent the message to the broker.)


#### Caution for Producer
If the Kafka client version uses a version (0.11+) that supports the header, but the broker that is connected to the Kafka client does not support the header (0.11-) 
Pinpoint can throws the following exception:
```
java.lang.IllegalArgumentException: Magic v1 does not support record headers
	at org.apache.kafka.common.record.MemoryRecordsBuilder.appendWithOffset(MemoryRecordsBuilder.java:410)
	at org.apache.kafka.common.record.MemoryRecordsBuilder.appendWithOffset(MemoryRecordsBuilder.java:449)
	at org.apache.kafka.common.record.MemoryRecordsBuilder.append(MemoryRecordsBuilder.java:506)
	at org.apache.kafka.common.record.MemoryRecordsBuilder.append(MemoryRecordsBuilder.java:529)
	at org.apache.kafka.clients.producer.internals.ProducerBatch.tryAppend(ProducerBatch.java:107)
	at org.apache.kafka.clients.producer.internals.RecordAccumulator.append(RecordAccumulator.java:223)
	at org.apache.kafka.clients.producer.KafkaProducer.doSend(KafkaProducer.java:864)
	at org.apache.kafka.clients.producer.KafkaProducer.send(KafkaProducer.java:803)
	at org.apache.kafka.clients.producer.KafkaProducer.send(KafkaProducer.java:690)
```

In this case, disable the Kafka plugin.
or 
The broker that is connected to the Kafka client must be updated to a version(0.11+) that supports the header.

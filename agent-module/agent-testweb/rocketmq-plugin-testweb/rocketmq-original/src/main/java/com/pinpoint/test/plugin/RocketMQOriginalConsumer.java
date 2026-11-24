/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pinpoint.test.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
/**
 * @author messi-gao
 */
@Component
public class RocketMQOriginalConsumer {
    private static final String PINPOINT_HEADER_PREFIX = "Pinpoint-";

    private final Logger logger = LogManager.getLogger(this.getClass());
    @Value("${namesrvAddr}")
    private String namesrvAddr;

    private DefaultMQPushConsumer consumer;

    @PostConstruct
    public void init() throws MQClientException {

        // Instantiate with specified consumer group name.
        consumer = new DefaultMQPushConsumer("your_consumer_group_name");

        // Specify name server addresses.
        consumer.setNamesrvAddr(namesrvAddr);

        // Subscribe one more more topics to consume.
        consumer.subscribe("TopicTest", "*");
        // Register callback to execute on arrival of messages fetched from brokers.
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                logger.info("Receive New Messages: {} {}", Thread.currentThread().getName(), msgs);
                for (MessageExt msg : msgs) {
                    // printed so that the propagation from the producer can be checked without a collector
                    System.out.println("Received message: " + new String(msg.getBody(), StandardCharsets.UTF_8)
                            + ", headers=" + pinpointHeaders(msg));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        //Launch the consumer instance.
        consumer.start();

        logger.info("Consumer Started.%n");
    }

    // the consumer threads are not daemons: without this the JVM stays alive after spring-boot:stop
    @PreDestroy
    public void destroy() {
        if (consumer != null) {
            consumer.shutdown();
        }
    }

    private static Map<String, String> pinpointHeaders(MessageExt msg) {
        Map<String, String> headers = new TreeMap<>();
        Map<String, String> properties = msg.getProperties();
        if (properties == null) {
            return headers;
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().startsWith(PINPOINT_HEADER_PREFIX)) {
                headers.put(entry.getKey(), entry.getValue());
            }
        }
        return headers;
    }
}

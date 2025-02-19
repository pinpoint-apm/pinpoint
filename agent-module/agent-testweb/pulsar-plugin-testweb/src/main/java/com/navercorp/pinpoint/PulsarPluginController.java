/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static org.apache.pulsar.client.api.SubscriptionInitialPosition.Earliest;

/**
 * @author zhouzixin@apache.org
 */
@RestController
public class PulsarPluginController {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final String topic = "pinpoint-test";
    private PulsarClient client;
    private Consumer<byte[]> consumer;

    @PostConstruct
    public void init() {
        try {
            client = PulsarClient.builder()
                    .serviceUrl("pulsar://localhost:6650")
                    .build();

            consumer = client.newConsumer()
                    .topic(topic)
                    .subscriptionName("pinpoint-subscription")
                    .subscriptionInitialPosition(Earliest)
                    .messageListener(new MessageInterceptor<>())
                    .subscribe();

            logger.info("Pulsar consumer initialized successfully.");
        } catch (Exception e) {
            logger.error("Pulsar service init failed.", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (consumer != null) {
                consumer.close();
            }
            if (client != null) {
                client.close();
            }
            logger.info("Pulsar resources closed.");
        } catch (Exception e) {
            logger.error("Error closing Pulsar resources.", e);
        }
    }

    @GetMapping("/send")
    public String send() {
        try (Producer<byte[]> producer = client.newProducer()
                .topic(topic)
                .create()) {
            producer.newMessage()
                    .key("test-key")
                    .value("send".getBytes())
                    .send();
            logger.info("Message sent successfully.");
            return "success";
        } catch (PulsarClientException e) {
            logger.error("Failed to send message.", e);
            return "error";
        }
    }

    class MessageInterceptor<T> implements MessageListener<T> {
        @Override
        public void received(final Consumer<T> consumer, final Message<T> msg) {
            try {
                logger.info(
                        "Received message: {}, key = {}, value = {}",
                        msg.getMessageId(),
                        msg.getKey(),
                        new String(msg.getData())
                );
                consumer.acknowledge(msg);
            } catch (Exception e) {
                logger.error("Failed to process message.", e);
                consumer.negativeAcknowledge(msg);
            }
        }
    }
}

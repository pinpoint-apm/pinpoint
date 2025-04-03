/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringKafkaApplication {
    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(SpringKafkaApplication.class, args);
//
//        MessageProducer producer = context.getBean(MessageProducer.class);
//        MessageListener listener = context.getBean(MessageListener.class);
//        /*
//         * Sending a Hello World message to topic 'baeldung'.
//         * Must be received by both listeners with group foo
//         * and bar with containerFactory fooKafkaListenerContainerFactory
//         * and barKafkaListenerContainerFactory respectively.
//         * It will also be received by the listener with
//         * headersKafkaListenerContainerFactory as container factory.
//         */
//        producer.sendMessage("Hello, World!");
//        listener.latch.await(10, TimeUnit.SECONDS);
//
//        /*
//         * Sending message to a topic with 5 partitions,
//         * each message to a different partition. But as per
//         * listener configuration, only the messages from
//         * partition 0 and 3 will be consumed.
//         */
//        for (int i = 0; i < 5; i++) {
//            producer.sendMessageToPartition("Hello To Partitioned Topic!", i);
//        }
//        listener.partitionLatch.await(10, TimeUnit.SECONDS);
//
//        /*
//         * Sending message to 'filtered' topic. As per listener
//         * configuration,  all messages with char sequence
//         * 'World' will be discarded.
//         */
//        producer.sendMessageToFiltered("Hello Baeldung!");
//        producer.sendMessageToFiltered("Hello World!");
//        listener.filterLatch.await(10, TimeUnit.SECONDS);
//
//        /*
//         * Sending message to 'greeting' topic. This will send
//         * and received a java object with the help of
//         * greetingKafkaListenerContainerFactory.
//         */
//        producer.sendGreetingMessage(new Greeting("Greetings", "World!"));
//        listener.greetingLatch.await(10, TimeUnit.SECONDS);
//
//        context.close();
    }

    @Bean
    public MessageProducer messageProducer() {
        return new MessageProducer();
    }

    @Bean
    public MessageListener messageListener() {
        return new MessageListener();
    }

    public static class MessageProducer {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        @Autowired
        private KafkaTemplate<String, Greeting> greetingKafkaTemplate;

        public void sendMessage(String message) {

            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(SpringKafkaPluginTestConstants.TOPIC_NAME, message);
            future.completable().whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata()
                            .offset() + "]");
                } else {
                    System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
                }
            });
        }

        public void sendMessageToPartition(String message, int partition) {
            kafkaTemplate.send(SpringKafkaPluginTestConstants.PARTITIONED_TOPIC_NAME, partition, null, message);
        }

        public void sendMessageToFiltered(String message) {
            kafkaTemplate.send(SpringKafkaPluginTestConstants.FILTERED_TOPIC_NAME, message);
        }

        public void sendGreetingMessage(Greeting greeting) {
            greetingKafkaTemplate.send(SpringKafkaPluginTestConstants.GREETING_TOPIC_NAME, greeting);
        }

        public void sendStreamMessage(String message) {
            kafkaTemplate.send("streamingTopic1", message);
        }
    }

    public static class MessageListener {

        private CountDownLatch latch = new CountDownLatch(3);

        private CountDownLatch partitionLatch = new CountDownLatch(2);

        private CountDownLatch filterLatch = new CountDownLatch(2);

        private CountDownLatch greetingLatch = new CountDownLatch(1);

        @KafkaListener(topics = SpringKafkaPluginTestConstants.TOPIC_NAME, groupId = "foo", containerFactory = "fooKafkaListenerContainerFactory")
        public void listenGroupFoo(String message) {
            throw new NullPointerException("null");
//            System.out.println("Received Message in group 'foo': " + message);
//            latch.countDown();
        }

        @KafkaListener(topics = SpringKafkaPluginTestConstants.TOPIC_NAME, groupId = "bar", containerFactory = "barKafkaListenerContainerFactory")
        public void listenGroupBar(String message) {
            System.out.println("Received Message in group 'bar': " + message);
            latch.countDown();
        }

        @KafkaListener(topics = SpringKafkaPluginTestConstants.TOPIC_NAME, containerFactory = "headersKafkaListenerContainerFactory")
        public void listenWithHeaders(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
            System.out.println("Received Message: " + message + " from partition: " + partition);
            latch.countDown();
        }

        @KafkaListener(topicPartitions = @TopicPartition(topic = SpringKafkaPluginTestConstants.PARTITIONED_TOPIC_NAME, partitions = { "0", "3" }), containerFactory = "partitionsKafkaListenerContainerFactory")
        public void listenToPartition(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
            System.out.println("Received Message: " + message + " from partition: " + partition);
            this.partitionLatch.countDown();
        }

        @KafkaListener(topics = SpringKafkaPluginTestConstants.FILTERED_TOPIC_NAME, containerFactory = "filterKafkaListenerContainerFactory")
        public void listenWithFilter(String message) {
            System.out.println("Received Message in filtered listener: " + message);
            this.filterLatch.countDown();
        }

        @KafkaListener(topics = SpringKafkaPluginTestConstants.GREETING_TOPIC_NAME, containerFactory = "greetingKafkaListenerContainerFactory")
        public void greetingListener(Greeting greeting) {
            System.out.println("Received greeting message: " + greeting);
            this.greetingLatch.countDown();
        }

    }

}

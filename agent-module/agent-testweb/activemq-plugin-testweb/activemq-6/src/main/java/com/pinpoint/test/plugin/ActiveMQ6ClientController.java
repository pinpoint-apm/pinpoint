/*
 * Copyright 2026 NAVER Corp.
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

import jakarta.jms.Connection;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.AsyncCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * ActiveMQ 6.x client through the JMS API without Spring JMS. send, sendAsync and sendAndReceive are the scenarios of
 * the activemq-5 app, so both lines can be compared.
 */
@RestController
public class ActiveMQ6ClientController {

    static final String LISTENER_QUEUE = "pinpoint.activemq6.client.listener";
    static final String RECEIVE_QUEUE = "pinpoint.activemq6.client.receive";
    static final String RECEIVE_BODY_QUEUE = "pinpoint.activemq6.client.receive-body";

    private static final long TIMEOUT_MILLIS = 3000;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Connection connection;
    private final String brokerUrl;

    public ActiveMQ6ClientController(@Qualifier(ActiveMQ6Config.CLIENT_CONNECTION) Connection connection,
                                     @Value("${spring.activemq.broker-url}") String brokerUrl) {
        this.connection = connection;
        this.brokerUrl = brokerUrl;
    }

    /**
     * Sends to the queue that {@link ActiveMQ6ClientListener} consumes.
     */
    @GetMapping("/activemq6/client/send")
    public String send() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            MessageProducer producer = session.createProducer(session.createQueue(LISTENER_QUEUE));
            producer.send(session.createTextMessage("client/send"));
            return "sent to " + LISTENER_QUEUE;
        } finally {
            session.close();
        }
    }

    /**
     * Sends with an ActiveMQ AsyncCallback to the queue that {@link ActiveMQ6ClientListener} consumes.
     */
    @GetMapping("/activemq6/client/sendAsync")
    public String sendAsync() throws Exception {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            Queue queue = session.createQueue(LISTENER_QUEUE);
            ActiveMQMessageProducer producer = (ActiveMQMessageProducer) session.createProducer(null);
            CompletableFuture<String> result = new CompletableFuture<>();
            producer.send(queue, session.createTextMessage("client/sendAsync"), new AsyncCallback() {
                @Override
                public void onSuccess() {
                    result.complete("sent asynchronously to " + LISTENER_QUEUE);
                }

                @Override
                public void onException(JMSException exception) {
                    logger.warn("Failed to send", exception);
                    result.completeExceptionally(exception);
                }
            });
            return result.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } finally {
            session.close();
        }
    }

    /**
     * Sends and then pulls the message back with MessageConsumer.receive, the receive path of the consumer.
     */
    @GetMapping("/activemq6/client/sendAndReceive")
    public String sendAndReceive() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            Queue queue = session.createQueue(RECEIVE_QUEUE);
            MessageConsumer consumer = session.createConsumer(queue);
            session.createProducer(queue).send(session.createTextMessage("client/sendAndReceive"));
            Message message = consumer.receive(TIMEOUT_MILLIS);
            if (message == null) {
                return "nothing received from " + RECEIVE_QUEUE;
            }
            return "received " + ((TextMessage) message).getText() + " headers=" + PinpointHeaders.of(message);
        } finally {
            session.close();
        }
    }

    /**
     * Sends and then pulls only the body back with JMSConsumer.receiveBody, which ActiveMQ implements from 6.4.0.
     * The body carries no message properties, so the trace headers cannot be shown here.
     */
    @GetMapping("/activemq6/client/sendAndReceiveBody")
    public String sendAndReceiveBody() {
        try (JMSContext context = new ActiveMQConnectionFactory(brokerUrl).createContext()) {
            Queue queue = context.createQueue(RECEIVE_BODY_QUEUE);
            JMSConsumer consumer = context.createConsumer(queue);
            context.createProducer().send(queue, "client/sendAndReceiveBody");
            String body = consumer.receiveBody(String.class, TIMEOUT_MILLIS);
            if (body == null) {
                return "nothing received from " + RECEIVE_BODY_QUEUE;
            }
            return "received body " + body;
        }
    }
}

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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * MessageListener registered through the JMS API without Spring JMS. Logs the trace headers of every message it
 * receives, see {@link PinpointHeaders}.
 */
@Component
public class ActiveMQ6ClientListener {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Connection connection;
    private Session session;

    public ActiveMQ6ClientListener(@Qualifier(ActiveMQ6Config.CLIENT_CONNECTION) Connection connection) {
        this.connection = connection;
    }

    @PostConstruct
    public void start() throws JMSException {
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue(ActiveMQ6ClientController.LISTENER_QUEUE));
        consumer.setMessageListener(message -> {
            try {
                logger.info("Received {} from {} headers={}", ((TextMessage) message).getText(), ActiveMQ6ClientController.LISTENER_QUEUE, PinpointHeaders.of(message));
            } catch (JMSException e) {
                logger.warn("Failed to read the message", e);
            }
        });
    }

    @PreDestroy
    public void stop() throws JMSException {
        if (session != null) {
            session.close();
        }
    }
}

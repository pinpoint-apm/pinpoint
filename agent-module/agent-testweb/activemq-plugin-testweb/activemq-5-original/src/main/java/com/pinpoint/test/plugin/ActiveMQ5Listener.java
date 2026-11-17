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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Consumes {@link ActiveMQ5Controller#LISTENER_QUEUE} through a MessageListener, the dispatch path of the consumer.
 */
@Component
public class ActiveMQ5Listener {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Connection connection;
    private Session session;

    public ActiveMQ5Listener(Connection connection) {
        this.connection = connection;
    }

    @PostConstruct
    public void start() throws JMSException {
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue(ActiveMQ5Controller.LISTENER_QUEUE));
        consumer.setMessageListener(message -> {
            try {
                logger.info("Received {}", ((TextMessage) message).getText());
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

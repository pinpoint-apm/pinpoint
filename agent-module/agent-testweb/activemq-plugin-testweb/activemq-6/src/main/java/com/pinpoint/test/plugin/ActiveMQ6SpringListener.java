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

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @JmsListener containers of Spring JMS. Logs the trace headers of every message it receives, see
 * {@link PinpointHeaders}.
 */
@Component
public class ActiveMQ6SpringListener {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Default listener container: polls with MessageConsumer.receive.
     */
    @JmsListener(destination = ActiveMQ6SpringController.LISTENER_QUEUE)
    public void onMessage(TextMessage message) throws JMSException {
        logger.info("Received {} from {} headers={}", message.getText(), ActiveMQ6SpringController.LISTENER_QUEUE, PinpointHeaders.of(message));
    }

    /**
     * Simple listener container: the consumer dispatches to a MessageListener.
     */
    @JmsListener(destination = ActiveMQ6SpringController.SIMPLE_LISTENER_QUEUE, containerFactory = ActiveMQ6Config.SIMPLE_CONTAINER_FACTORY)
    public void onSimpleMessage(TextMessage message) throws JMSException {
        logger.info("Received {} from {} headers={}", message.getText(), ActiveMQ6SpringController.SIMPLE_LISTENER_QUEUE, PinpointHeaders.of(message));
    }
}

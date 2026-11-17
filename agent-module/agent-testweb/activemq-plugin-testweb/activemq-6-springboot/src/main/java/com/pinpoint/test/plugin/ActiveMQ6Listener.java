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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ActiveMQ6Listener {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Default listener container: polls with MessageConsumer.receive.
     */
    @JmsListener(destination = ActiveMQ6Controller.LISTENER_QUEUE)
    public void onMessage(String text) {
        logger.info("Received {} from {}", text, ActiveMQ6Controller.LISTENER_QUEUE);
    }

    /**
     * Simple listener container: the consumer dispatches to a MessageListener.
     */
    @JmsListener(destination = ActiveMQ6Controller.SIMPLE_LISTENER_QUEUE, containerFactory = ActiveMQ6Config.SIMPLE_CONTAINER_FACTORY)
    public void onSimpleMessage(String text) {
        logger.info("Received {} from {}", text, ActiveMQ6Controller.SIMPLE_LISTENER_QUEUE);
    }
}

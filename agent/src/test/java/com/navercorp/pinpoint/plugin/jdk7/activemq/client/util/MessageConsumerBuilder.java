/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.jdk7.activemq.client.util;

import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.advisory.ConsumerEvent;
import org.apache.activemq.advisory.ConsumerEventSource;
import org.apache.activemq.advisory.ConsumerListener;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author HyunGil Jeong
 */
public class MessageConsumerBuilder {

    private final ActiveMQSession session;
    private final Destination destination;
    private MessageListener messageListener;
    private boolean waitTillStarted = false;

    public MessageConsumerBuilder(ActiveMQSession session, Destination destination) {
        this.session = session;
        this.destination = destination;
    }

    public MessageConsumerBuilder withMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
        return this;
    }

    public MessageConsumerBuilder waitTillStarted() {
        this.waitTillStarted = true;
        return this;
    }

    public MessageConsumer build() throws Exception {
        MessageConsumer consumer = null;
        if (waitTillStarted) {
            ConsumerEventSource consumerEventSource = new ConsumerEventSource(session.getConnection(), destination);
            final CountDownLatch latch = new CountDownLatch(1);
            consumerEventSource.setConsumerListener(new ConsumerListener() {
                @Override
                public void onConsumerEvent(ConsumerEvent event) {
                    latch.countDown();
                }
            });
            try {
                consumerEventSource.start();
                consumer = this.session.createConsumer(this.destination);
                if (!latch.await(5L, TimeUnit.SECONDS)) {
                    throw new TimeoutException("Timed out waiting for MessageConsumer start event.");
                }
            } finally {
                consumerEventSource.stop();
            }
        } else {
            consumer = this.session.createConsumer(this.destination);
        }
        if (this.messageListener != null) {
            consumer.setMessageListener(this.messageListener);
        }
        return consumer;
    }

    public static class ForQueue extends MessageConsumerBuilder {
        public ForQueue(ActiveMQSession session, String queueName) throws JMSException {
            super(session, session.createQueue(queueName));
        }
    }

    public static class ForTopic extends MessageConsumerBuilder {
        public ForTopic(ActiveMQSession session, String topicName) throws JMSException {
            super(session, session.createQueue(topicName));
        }
    }
}

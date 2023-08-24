/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client.util;

import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.advisory.ProducerEvent;
import org.apache.activemq.advisory.ProducerEventSource;
import org.apache.activemq.advisory.ProducerListener;

import javax.jms.Destination;
import javax.jms.MessageProducer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author HyunGil Jeong
 */
public class MessageProducerBuilder {

    private final ActiveMQSession session;
    private final Destination destination;
    private boolean waitTillStarted = false;

    public MessageProducerBuilder(ActiveMQSession session, Destination destination) {
        this.session = session;
        this.destination = destination;
    }

    public MessageProducerBuilder waitTillStarted() {
        this.waitTillStarted = true;
        return this;
    }

    public MessageProducer build() throws Exception {
        if (waitTillStarted) {
            ProducerEventSource producerEventSource = new ProducerEventSource(session.getConnection(), destination);
            final CountDownLatch latch = new CountDownLatch(1);
            producerEventSource.setProducerListener(new ProducerListener() {
                @Override
                public void onProducerEvent(ProducerEvent event) {
                    latch.countDown();
                }
            });
            MessageProducer producer = null;
            try {
                producerEventSource.start();
                producer = this.session.createProducer(this.destination);
                if (!latch.await(5L, TimeUnit.SECONDS)) {
                    throw new TimeoutException("Timed out waiting for MessageProducer start event.");
                }
            } finally {
                producerEventSource.stop();
            }
            return producer;
        } else {
            return this.session.createProducer(this.destination);
        }
    }
}

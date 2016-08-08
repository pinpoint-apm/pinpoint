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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * @author HyunGil Jeong
 */
public abstract class AssertMessageListener<T> implements MessageListener {

    private final CountDownLatch consumeLatch;
    private final Collection<Throwable> throwableHolder;
    private final T expectedMessage;

    protected AssertMessageListener(CountDownLatch consumeLatch, Collection<Throwable> throwableHolder, T expectedMessage) {
        this.consumeLatch = consumeLatch;
        this.throwableHolder = throwableHolder;
        this.expectedMessage = expectedMessage;
    }

    @Override
    public final void onMessage(Message message) {
        try {
            assertMessage(this.expectedMessage, message);
        } catch (Throwable t) {
            this.throwableHolder.add(t);
        } finally {
            this.consumeLatch.countDown();
        }
    }

    protected abstract void assertMessage(T expectedMessage, Message actual) throws JMSException;
}

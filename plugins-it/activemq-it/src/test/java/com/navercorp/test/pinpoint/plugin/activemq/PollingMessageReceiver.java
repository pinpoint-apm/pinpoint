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

package com.navercorp.test.pinpoint.plugin.activemq;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author HyunGil Jeong
 */
public class PollingMessageReceiver {

    public static final long RECEIVE_TIMEOUT_MS = 100L;
    private final Thread pollerThread;
    private final Poller poller;

    public PollingMessageReceiver(MessageConsumer consumer, MessageReceiveHandler messageReceiveHandler) {
        poller = new Poller(consumer, messageReceiveHandler);
        pollerThread = new Thread(poller);
        pollerThread.setDaemon(true);
    }

    public void start() {
        pollerThread.start();
    }

    public void stop() throws InterruptedException {
        if (pollerThread.isAlive()) {
            poller.stop();
            pollerThread.join(RECEIVE_TIMEOUT_MS);
        }
    }

    public Exception getException() {
        return poller.getException();
    }

    private static class Poller implements Runnable {

        private final MessageConsumer consumer;
        private final MessageReceiveHandler messageReceiveHandler;
        private final AtomicReference<Exception> exception = new AtomicReference<Exception>();
        private volatile boolean isRunning = true;

        private Poller(MessageConsumer consumer, MessageReceiveHandler messageReceiveHandler) {
            this.consumer = consumer;
            this.messageReceiveHandler = messageReceiveHandler;
        }

        @Override
        public void run() {
            try {
                while (isRunning) {
                    Message message = consumer.receive(RECEIVE_TIMEOUT_MS);
                    if (message != null) {
                        messageReceiveHandler.handleMessage(message);
                    }
                }
            } catch (Exception e) {
                exception.set(e);
            }
        }

        public void stop() {
            this.isRunning = false;
        }

        public Exception getException() {
            return exception.get();
        }
    }

}

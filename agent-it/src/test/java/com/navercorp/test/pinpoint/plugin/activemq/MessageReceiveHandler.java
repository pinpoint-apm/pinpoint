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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
public class MessageReceiveHandler {

    private final CountDownLatch messageReceivedLatch = new CountDownLatch(1);
    private final MessagePrinter messageLogger = new MessagePrinter();

    public void handleMessage(Message message) {
        messageLogger.printMessage(message);
        messageReceivedLatch.countDown();
    }

    public boolean await(long timeoutMs) throws InterruptedException {
        return messageReceivedLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
    }
}

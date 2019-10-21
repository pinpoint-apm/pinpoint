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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

/**
 * Helper class that acts as an entry point for a request to consume messages.
 *
 * @author HyunGil Jeong
 */
public class MessageReceiver {

    private final MessagePrinter messageLogger = new MessagePrinter();
    private final MessageConsumer messageConsumer;

    public MessageReceiver(MessageConsumer messageConsumer) {
        if (messageConsumer == null) {
            throw new NullPointerException("messageConsumer");
        }
        this.messageConsumer = messageConsumer;
    }

    public void receiveMessage() throws JMSException {
        Message message = messageConsumer.receive();
        messageLogger.printMessage(message);
    }

    public void receiveMessage(long timeout) throws JMSException {
        Message message = messageConsumer.receive(timeout);
        messageLogger.printMessage(message);
    }
}

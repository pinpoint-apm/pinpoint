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

import junit.framework.AssertionFailedError;
import org.junit.Assert;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * @author HyunGil Jeong
 */
public class AssertTextMessageListener extends AssertMessageListener<TextMessage> {

    public AssertTextMessageListener(CountDownLatch consumeLatch, Collection<Throwable> throwableHolder, TextMessage expectedMessage) {
        super(consumeLatch, throwableHolder, expectedMessage);
    }

    @Override
    protected void assertMessage(TextMessage expected, Message actual) throws JMSException {
        if (!(actual instanceof TextMessage)) {
            throw new AssertionFailedError("Expected TextMessage but got " + actual.getClass().getSimpleName());
        }
        String actualText = ((TextMessage) actual).getText();
        Assert.assertEquals(expected.getText(), actualText);
    }
}

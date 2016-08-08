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

import org.apache.activemq.ActiveMQConnection;

import javax.jms.JMSException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class TestBrokerService {

    private final Deque<TestBroker> testBrokers;
    private final Map<String, TestBroker> testBrokerMap = new HashMap<String, TestBroker>();

    TestBrokerService(List<TestBroker> testBrokers) {
        if (testBrokers == null) {
            throw new NullPointerException("testBrokers must not be null");
        }
        if (testBrokers.isEmpty()) {
            throw new IllegalArgumentException("testBrokers must not be empty");
        }
        this.testBrokers = new ArrayDeque<TestBroker>(testBrokers);
        for (TestBroker testBroker : testBrokers) {
            this.testBrokerMap.put(testBroker.getBrokerName(), testBroker);
        }
    }

    ActiveMQConnection getConnection(String brokerName, String connectUri) throws JMSException {
        return this.testBrokerMap.get(brokerName).getConnection(connectUri);
    }

    void start() throws Exception {
        for (TestBroker testBroker : this.testBrokers) {
            if (!testBroker.start()) {
                throw new RuntimeException("Error starting broker [" + testBroker.getBrokerName() + "]");
            }
        }
    }

    void stop() throws Exception {
        // stop in reverse sequence
        Iterator<TestBroker> reverseIterator = this.testBrokers.descendingIterator();
        while (reverseIterator.hasNext()) {
            TestBroker testBroker = reverseIterator.next();
            testBroker.stop();
        }
    }
}

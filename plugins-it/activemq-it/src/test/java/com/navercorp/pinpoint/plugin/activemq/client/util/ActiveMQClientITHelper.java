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

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQSession;

import javax.jms.JMSException;
import javax.jms.Session;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQClientITHelper {

    private static TestBrokerService BROKER_SERVICE;

    private ActiveMQClientITHelper() {}

    public static void startBrokers(List<TestBroker> brokers) throws Exception {
        BROKER_SERVICE = new TestBrokerService(brokers);
        BROKER_SERVICE.start();
    }

    public static void stopBrokers() throws Exception {
        BROKER_SERVICE.stop();
    }

    public static ActiveMQSession createSession(String brokerName, String brokerUrl) throws JMSException {
        return createSession(brokerName, brokerUrl, false, Session.AUTO_ACKNOWLEDGE);
    }

    public static ActiveMQSession createSession(String brokerName, String brokerUrl, boolean transacted, int acknowledgeMode) throws JMSException {
        ActiveMQConnection connection = BROKER_SERVICE.getConnection(brokerName, brokerUrl);
        return (ActiveMQSession) connection.createSession(transacted, acknowledgeMode);
    }
}

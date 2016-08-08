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
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

import javax.jms.JMSException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class TestBroker {

    public static final String DEFAULT_BROKER_URL = "tcp://127.0.0.1:61616";

    private final String brokerName;
    private final BrokerService brokerService;
    private final Map<String, ActiveMQConnectionFactory> connectionFactories;
    private final Map<String, ActiveMQConnection> connections = new HashMap<String, ActiveMQConnection>();

    private TestBroker(String brokerName, BrokerService brokerService, Map<String, ActiveMQConnectionFactory> connectionFactories) throws Exception {
        this.brokerName = brokerName;
        this.brokerService = brokerService;
        this.connectionFactories = Collections.unmodifiableMap(connectionFactories);
    }

    String getBrokerName() {
        return this.brokerName;
    }

    ActiveMQConnection getConnection(String connectUri) throws JMSException {
        if (!this.connections.containsKey(connectUri)) {
            throw new IllegalArgumentException("Connection for connectUri [" + connectUri + "] does not exist");
        }
        return this.connections.get(connectUri);
    }

    boolean start() throws Exception {
        this.brokerService.start();
        // it might be better to wait until the broker has fully started using BrokerService#waitUntilStarted
        // but this method was only introduced in 5.3.0
        for (Map.Entry<String, ActiveMQConnectionFactory> e : this.connectionFactories.entrySet()) {
            String connectUri = e.getKey();
            ActiveMQConnectionFactory connectionFactory = e.getValue();
            ActiveMQConnection connection = (ActiveMQConnection) connectionFactory.createConnection();
            connection.setClientID("client_" + connectUri);
            connection.start();
            this.connections.put(connectUri, connection);
        }
        return true;
    }

    void stop() throws Exception {
        for (Map.Entry<String, ActiveMQConnection> e : this.connections.entrySet()) {
            ActiveMQConnection connection = e.getValue();
            connection.close();
        }
        this.brokerService.stop();
        this.brokerService.waitUntilStopped();
    }

    public static class TestBrokerBuilder {

        private final String brokerName;
        private final Set<String> connectors = new HashSet<String>();
        private final Set<String> networkConnectors = new HashSet<String>();

        public TestBrokerBuilder(String brokerName) {
            if (brokerName == null) {
                throw new NullPointerException("brokerName must not be empty");
            }
            this.brokerName = brokerName;
        }

        public TestBrokerBuilder addConnector(String bindAddress) {
            this.connectors.add(bindAddress);
            return this;
        }

        public TestBrokerBuilder addNetworkConnector(String discoveryAddress) {
            this.networkConnectors.add(discoveryAddress);
            return this;
        }

        public TestBroker build() throws Exception {
            if (this.connectors.isEmpty()) {
                this.connectors.add(DEFAULT_BROKER_URL);
            }
            BrokerService brokerService = new BrokerService();
            brokerService.setBrokerName(this.brokerName);
            brokerService.setPersistent(false);
            brokerService.setUseJmx(false);
            Map<String, ActiveMQConnectionFactory> connectionFactories = new HashMap<String, ActiveMQConnectionFactory>();
            for (String bindAddress : this.connectors) {
                TransportConnector connector = brokerService.addConnector(bindAddress);
                connectionFactories.put(bindAddress, new ActiveMQConnectionFactory(connector.getConnectUri()));
            }
            for (String discoveryAddress : this.networkConnectors) {
                brokerService.addNetworkConnector(discoveryAddress);
            }
            return new TestBroker(this.brokerName, brokerService, connectionFactories);
        }
    }
}

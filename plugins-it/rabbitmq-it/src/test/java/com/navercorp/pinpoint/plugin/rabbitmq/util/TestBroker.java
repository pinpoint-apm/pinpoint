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

package com.navercorp.pinpoint.plugin.rabbitmq.util;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;

import java.io.File;

/**
 * @author HyunGil Jeong
 */
public class TestBroker {

    static {
        setSystemProperties();
    }

    private final Broker broker = new Broker();

    private static void setSystemProperties() {
        String qpidWorkDir = new File(System.getProperty("java.io.tmpdir"), "qpidworktmp").getAbsolutePath();
        System.setProperty("qpid.work_dir", qpidWorkDir);
        System.setProperty("qpid.initialConfigurationLocation", "rabbitmq/qpid/qpid-config.json");
        // Suppress derby.log
        System.setProperty("derby.stream.error.field", "DerbyUtil.DEV_NULL");
    }

    public void start() throws Exception {
        BrokerOptions brokerOptions = new BrokerOptions();
        brokerOptions.setConfigProperty("qpid.amqp_port", String.valueOf(RabbitMQTestConstants.BROKER_PORT));
        brokerOptions.setConfigurationStoreType("Memory");
        brokerOptions.setStartupLoggedToSystemOut(false);
        start(brokerOptions);
    }

    public void start(BrokerOptions brokerOptions) throws Exception {
        broker.startup(brokerOptions);
    }

    public void shutdown() {
        broker.shutdown();
    }
}

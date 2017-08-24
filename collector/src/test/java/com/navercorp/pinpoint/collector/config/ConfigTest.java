/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.util.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class ConfigTest {

    @Test
    public void deprecatedConfigTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-deprecated-pinpoint-collector.properties");

        DeprecatedConfiguration deprecatedConfig = new DeprecatedConfiguration(properties);

        AgentBaseDataReceiverConfiguration agentBaseDataReceiverConfig = new AgentBaseDataReceiverConfiguration(properties, deprecatedConfig);

        Assert.assertTrue(deprecatedConfig.isSetTcpListenIp());
        Assert.assertEquals(agentBaseDataReceiverConfig.getBindIp(), deprecatedConfig.getTcpListenIp());

        Assert.assertTrue(deprecatedConfig.isSetTcpListenPort());
        Assert.assertEquals(agentBaseDataReceiverConfig.getBindPort(), deprecatedConfig.getTcpListenPort());

        Assert.assertTrue(deprecatedConfig.isSetTcpWorkerThread());
        Assert.assertEquals(agentBaseDataReceiverConfig.getWorkerThreadSize(), deprecatedConfig.getTcpWorkerThread());

        Assert.assertTrue(deprecatedConfig.isSetTcpWorkerQueueSize());
        Assert.assertEquals(agentBaseDataReceiverConfig.getWorkerQueueSize(), deprecatedConfig.getTcpWorkerQueueSize());

        Assert.assertTrue(deprecatedConfig.isSetTcpWorkerMonitor());
        Assert.assertEquals(agentBaseDataReceiverConfig.isWorkerMonitorEnable(), deprecatedConfig.isTcpWorkerMonitor());


        StatReceiverConfiguration statReceiverConfig = new StatReceiverConfiguration(properties, deprecatedConfig);

        Assert.assertTrue(deprecatedConfig.isSetUdpStatListenIp());
        Assert.assertEquals(statReceiverConfig.getUdpBindIp(), deprecatedConfig.getUdpStatListenIp());

        Assert.assertTrue(deprecatedConfig.isSetUdpSpanListenPort());
        Assert.assertEquals(statReceiverConfig.getUdpBindPort(), deprecatedConfig.getUdpStatListenPort());

        Assert.assertTrue(deprecatedConfig.isSetUdpStatWorkerThread());
        Assert.assertEquals(statReceiverConfig.getWorkerThreadSize(), deprecatedConfig.getUdpStatWorkerThread());

        Assert.assertTrue(deprecatedConfig.isSetUdpStatWorkerQueueSize());
        Assert.assertEquals(statReceiverConfig.getWorkerQueueSize(), deprecatedConfig.getUdpStatWorkerQueueSize());

        Assert.assertTrue(deprecatedConfig.isSetUdpStatWorkerMonitor());
        Assert.assertEquals(statReceiverConfig.isWorkerMonitorEnable(), deprecatedConfig.isUdpStatWorkerMonitor());

        Assert.assertTrue(deprecatedConfig.isSetUdpStatSocketReceiveBufferSize());
        Assert.assertEquals(statReceiverConfig.getUdpReceiveBufferSize(), deprecatedConfig.getUdpStatSocketReceiveBufferSize());

        Assert.assertFalse(statReceiverConfig.isTcpEnable());
        Assert.assertEquals(CollectorConfiguration.DEFAULT_LISTEN_IP, statReceiverConfig.getTcpBindIp());
        Assert.assertTrue(statReceiverConfig.getTcpBindPort() == -1);


        SpanReceiverConfiguration spanReceiverConfig = new SpanReceiverConfiguration(properties, deprecatedConfig);

        Assert.assertTrue(deprecatedConfig.isSetUdpSpanListenIp());
        Assert.assertEquals(spanReceiverConfig.getUdpBindIp(), deprecatedConfig.getUdpSpanListenIp());

        Assert.assertTrue(deprecatedConfig.isSetUdpSpanListenPort());
        Assert.assertEquals(spanReceiverConfig.getUdpBindPort(), deprecatedConfig.getUdpSpanListenPort());

        Assert.assertTrue(deprecatedConfig.isSetUdpSpanWorkerThread());
        Assert.assertEquals(spanReceiverConfig.getWorkerThreadSize(), deprecatedConfig.getUdpSpanWorkerThread());

        Assert.assertTrue(deprecatedConfig.isSetUdpSpanWorkerQueueSize());
        Assert.assertEquals(spanReceiverConfig.getWorkerQueueSize(), deprecatedConfig.getUdpSpanWorkerQueueSize());

        Assert.assertTrue(deprecatedConfig.isSetUdpSpanWorkerMonitor());
        Assert.assertEquals(spanReceiverConfig.isWorkerMonitorEnable(), deprecatedConfig.isUdpSpanWorkerMonitor());

        Assert.assertTrue(deprecatedConfig.isSetUdpSpanSocketReceiveBufferSize());
        Assert.assertEquals(spanReceiverConfig.getUdpReceiveBufferSize(), deprecatedConfig.getUdpSpanSocketReceiveBufferSize());

        Assert.assertFalse(spanReceiverConfig.isTcpEnable());
        Assert.assertEquals(CollectorConfiguration.DEFAULT_LISTEN_IP, spanReceiverConfig.getTcpBindIp());
        Assert.assertTrue(spanReceiverConfig.getTcpBindPort() == -1);
    }

    @Test
    public void configTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-pinpoint-collector.properties");

        DeprecatedConfiguration deprecatedConfig = new DeprecatedConfiguration(properties);
        Assert.assertFalse(deprecatedConfig.isSetTcpListenIp());
        Assert.assertFalse(deprecatedConfig.isSetTcpListenPort());
        Assert.assertFalse(deprecatedConfig.isSetTcpWorkerThread());
        Assert.assertFalse(deprecatedConfig.isSetTcpWorkerQueueSize());
        Assert.assertFalse(deprecatedConfig.isSetTcpWorkerMonitor());
        Assert.assertFalse(deprecatedConfig.isSetUdpStatListenIp());
        Assert.assertFalse(deprecatedConfig.isSetUdpSpanListenPort());
        Assert.assertFalse(deprecatedConfig.isSetUdpStatWorkerThread());
        Assert.assertFalse(deprecatedConfig.isSetUdpStatWorkerQueueSize());
        Assert.assertFalse(deprecatedConfig.isSetUdpStatWorkerMonitor());
        Assert.assertFalse(deprecatedConfig.isSetUdpStatSocketReceiveBufferSize());
        Assert.assertFalse(deprecatedConfig.isSetUdpSpanListenIp());
        Assert.assertFalse(deprecatedConfig.isSetUdpSpanListenPort());
        Assert.assertFalse(deprecatedConfig.isSetUdpSpanWorkerThread());
        Assert.assertFalse(deprecatedConfig.isSetUdpSpanWorkerQueueSize());
        Assert.assertFalse(deprecatedConfig.isSetUdpSpanWorkerMonitor());
        Assert.assertFalse(deprecatedConfig.isSetUdpSpanSocketReceiveBufferSize());

        AgentBaseDataReceiverConfiguration agentBaseDataReceiverConfig = new AgentBaseDataReceiverConfiguration(properties, deprecatedConfig);
        Assert.assertEquals(agentBaseDataReceiverConfig.getBindIp(), "0.0.0.2");
        Assert.assertEquals(agentBaseDataReceiverConfig.getBindPort(), 39994);
        Assert.assertEquals(agentBaseDataReceiverConfig.getWorkerThreadSize(), 33);
        Assert.assertEquals(agentBaseDataReceiverConfig.getWorkerQueueSize(), 29);
        Assert.assertEquals(agentBaseDataReceiverConfig.isWorkerMonitorEnable(), true);

        StatReceiverConfiguration statReceiverConfig = new StatReceiverConfiguration(properties, deprecatedConfig);
        Assert.assertEquals(statReceiverConfig.isUdpEnable(), false);
        Assert.assertEquals(statReceiverConfig.getUdpBindIp(), "0.0.0.1");
        Assert.assertEquals(statReceiverConfig.getUdpBindPort(), 39995);
        Assert.assertEquals(statReceiverConfig.getUdpReceiveBufferSize(), 419);
        Assert.assertEquals(statReceiverConfig.isTcpEnable(), true);
        Assert.assertEquals(statReceiverConfig.getTcpBindIp(), "0.0.0.2");
        Assert.assertEquals(statReceiverConfig.getTcpBindPort(), 39996);
        Assert.assertEquals(statReceiverConfig.getWorkerThreadSize(), 2);
        Assert.assertEquals(statReceiverConfig.getWorkerQueueSize(), 3);
        Assert.assertEquals(statReceiverConfig.isWorkerMonitorEnable(), true);

        SpanReceiverConfiguration spanReceiverConfig = new SpanReceiverConfiguration(properties, deprecatedConfig);
        Assert.assertEquals(spanReceiverConfig.isUdpEnable(), true);
        Assert.assertEquals(spanReceiverConfig.getUdpBindIp(), "0.0.0.3");
        Assert.assertEquals(spanReceiverConfig.getUdpBindPort(), 39997);
        Assert.assertEquals(spanReceiverConfig.getUdpReceiveBufferSize(), 568);
        Assert.assertEquals(spanReceiverConfig.isTcpEnable(), false);
        Assert.assertEquals(spanReceiverConfig.getTcpBindIp(), "0.0.0.4");
        Assert.assertEquals(spanReceiverConfig.getTcpBindPort(), 39998);
        Assert.assertEquals(spanReceiverConfig.getWorkerThreadSize(), 3);
        Assert.assertEquals(spanReceiverConfig.getWorkerQueueSize(), 4);
        Assert.assertEquals(spanReceiverConfig.isWorkerMonitorEnable(), false);
    }

}

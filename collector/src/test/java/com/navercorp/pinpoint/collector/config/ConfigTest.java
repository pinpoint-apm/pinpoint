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
    public void configTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("test-pinpoint-collector.properties");

        AgentBaseDataReceiverConfiguration agentBaseDataReceiverConfig = new AgentBaseDataReceiverConfiguration(properties);
        Assert.assertEquals(agentBaseDataReceiverConfig.getBindIp(), "0.0.0.2");
        Assert.assertEquals(agentBaseDataReceiverConfig.getBindPort(), 39994);
        Assert.assertEquals(agentBaseDataReceiverConfig.getWorkerThreadSize(), 33);
        Assert.assertEquals(agentBaseDataReceiverConfig.getWorkerQueueSize(), 29);
        Assert.assertTrue(agentBaseDataReceiverConfig.isWorkerMonitorEnable());

        StatReceiverConfiguration statReceiverConfig = new StatReceiverConfiguration(properties);
        Assert.assertFalse(statReceiverConfig.isUdpEnable());
        Assert.assertEquals(statReceiverConfig.getUdpBindIp(), "0.0.0.1");
        Assert.assertEquals(statReceiverConfig.getUdpBindPort(), 39995);
        Assert.assertEquals(statReceiverConfig.getUdpReceiveBufferSize(), 419);
        Assert.assertTrue(statReceiverConfig.isTcpEnable());
        Assert.assertEquals(statReceiverConfig.getTcpBindIp(), "0.0.0.2");
        Assert.assertEquals(statReceiverConfig.getTcpBindPort(), 39996);
        Assert.assertEquals(statReceiverConfig.getWorkerThreadSize(), 2);
        Assert.assertEquals(statReceiverConfig.getWorkerQueueSize(), 3);
        Assert.assertTrue(statReceiverConfig.isWorkerMonitorEnable());

        SpanReceiverConfiguration spanReceiverConfig = new SpanReceiverConfiguration(properties);
        Assert.assertTrue(spanReceiverConfig.isUdpEnable());
        Assert.assertEquals(spanReceiverConfig.getUdpBindIp(), "0.0.0.3");
        Assert.assertEquals(spanReceiverConfig.getUdpBindPort(), 39997);
        Assert.assertEquals(spanReceiverConfig.getUdpReceiveBufferSize(), 568);
        Assert.assertFalse(spanReceiverConfig.isTcpEnable());
        Assert.assertEquals(spanReceiverConfig.getTcpBindIp(), "0.0.0.4");
        Assert.assertEquals(spanReceiverConfig.getTcpBindPort(), 39998);
        Assert.assertEquals(spanReceiverConfig.getWorkerThreadSize(), 3);
        Assert.assertEquals(spanReceiverConfig.getWorkerQueueSize(), 4);
        Assert.assertFalse(spanReceiverConfig.isWorkerMonitorEnable());
    }

}

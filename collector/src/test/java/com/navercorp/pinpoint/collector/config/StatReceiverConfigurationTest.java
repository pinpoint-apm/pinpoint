/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@TestPropertySource(locations = "classpath:test-pinpoint-collector.properties")
@ContextConfiguration(classes = StatReceiverConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class StatReceiverConfigurationTest {

    @Autowired
    StatReceiverConfiguration configuration;

    @Test
    public void properties() throws Exception {

        Assert.assertFalse(configuration.isUdpEnable());
        Assert.assertEquals(configuration.getUdpBindIp(), "0.0.0.1");
        Assert.assertEquals(configuration.getUdpBindPort(), 39995);
        Assert.assertEquals(configuration.getUdpReceiveBufferSize(), 419);
        Assert.assertTrue(configuration.isTcpEnable());
        Assert.assertEquals(configuration.getTcpBindIp(), "0.0.0.2");
        Assert.assertEquals(configuration.getTcpBindPort(), 39996);
        Assert.assertEquals(configuration.getWorkerThreadSize(), 2);
        Assert.assertEquals(configuration.getWorkerQueueSize(), 3);
        Assert.assertTrue(configuration.isWorkerMonitorEnable());

    }
}
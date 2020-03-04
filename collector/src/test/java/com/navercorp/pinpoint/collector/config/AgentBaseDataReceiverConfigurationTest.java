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
@ContextConfiguration(classes = AgentBaseDataReceiverConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AgentBaseDataReceiverConfigurationTest {

    @Autowired
    AgentBaseDataReceiverConfiguration configuration;

    @Test
    public void properties() throws Exception {

        Assert.assertEquals(configuration.getBindIp(), "0.0.0.2");
        Assert.assertEquals(configuration.getBindPort(), 39994);
        Assert.assertEquals(configuration.getWorkerThreadSize(), 33);
        Assert.assertEquals(configuration.getWorkerQueueSize(), 29);
        Assert.assertTrue(configuration.isWorkerMonitorEnable());

    }
}
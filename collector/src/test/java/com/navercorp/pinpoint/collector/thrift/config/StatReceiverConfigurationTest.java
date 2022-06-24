/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.thrift.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestPropertySource(locations = "classpath:test-pinpoint-collector.properties")
@ContextConfiguration(classes = StatReceiverConfiguration.class)
@ExtendWith(SpringExtension.class)
public class StatReceiverConfigurationTest {

    @Autowired
    StatReceiverConfiguration configuration;

    @Test
    public void properties() {

        Assertions.assertFalse(configuration.isUdpEnable());
        Assertions.assertEquals(configuration.getUdpBindIp(), "0.0.0.1");
        Assertions.assertEquals(configuration.getUdpBindPort(), 39995);
        Assertions.assertEquals(configuration.getUdpReceiveBufferSize(), 419);
        Assertions.assertTrue(configuration.isTcpEnable());
        Assertions.assertEquals(configuration.getTcpBindIp(), "0.0.0.2");
        Assertions.assertEquals(configuration.getTcpBindPort(), 39996);
        Assertions.assertEquals(configuration.getWorkerThreadSize(), 2);
        Assertions.assertEquals(configuration.getWorkerQueueSize(), 3);
        Assertions.assertTrue(configuration.isWorkerMonitorEnable());

    }
}
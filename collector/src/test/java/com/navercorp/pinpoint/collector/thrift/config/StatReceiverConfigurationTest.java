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
@ContextConfiguration(classes = StatReceiverProperties.class)
@ExtendWith(SpringExtension.class)
public class StatReceiverConfigurationTest {

    @Autowired
    StatReceiverProperties properties;

    @Test
    public void properties() {

        Assertions.assertFalse(properties.isUdpEnable());
        Assertions.assertEquals(properties.getUdpBindIp(), "0.0.0.1");
        Assertions.assertEquals(properties.getUdpBindPort(), 39995);
        Assertions.assertEquals(properties.getUdpReceiveBufferSize(), 419);
        Assertions.assertTrue(properties.isTcpEnable());
        Assertions.assertEquals(properties.getTcpBindIp(), "0.0.0.2");
        Assertions.assertEquals(properties.getTcpBindPort(), 39996);
        Assertions.assertEquals(properties.getWorkerThreadSize(), 2);
        Assertions.assertEquals(properties.getWorkerQueueSize(), 3);
        Assertions.assertTrue(properties.isWorkerMonitorEnable());

    }
}
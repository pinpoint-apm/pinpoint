/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.testcase.util.SocketUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import test.pinpoint.plugin.kafka.Kafka2UnitServer;
import test.pinpoint.plugin.kafka.KafkaUnitServer;

public abstract class KafkaClient2ITBase extends KafkaClientITBase {

    private static KafkaUnitServer kafkaUnitServer;

    @BeforeClass
    public static void beforeClass() {
        final int zkPort = SocketUtils.findAvailableTcpPort(10000, 19999);
        final int brokerPort = SocketUtils.findAvailableTcpPort(20000, 29999);
        kafkaUnitServer = new Kafka2UnitServer(zkPort, brokerPort);

        kafkaUnitServer.startup();
        TEST_CONSUMER.start();
    }

    @AfterClass
    public static void afterClass() throws InterruptedException {
        TEST_CONSUMER.shutdown();
        kafkaUnitServer.shutdown();
    }
}

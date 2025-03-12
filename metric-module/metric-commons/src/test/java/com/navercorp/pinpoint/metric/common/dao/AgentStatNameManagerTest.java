/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.metric.common.dao;

import org.apache.kafka.clients.producer.internals.BuiltInPartitioner;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author minwoo-jung
 */
class AgentStatNameManagerTest {

    private final Logger logger = LogManager.getLogger(AgentStatNameManagerTest.class.getName());

    @Test
    public void kafkaPartitionForStringSortKeyTest() {
        int numPartitions = 32;
        try (Serializer<String> keySerializer = new StringSerializer()) {
            byte[] keyBytes = keySerializer.serialize("inspector-stat", "applicationName#AgentId#directBuffer");
            int partition = BuiltInPartitioner.partitionForKey(keyBytes, numPartitions);
            logger.debug(partition);
//        assertEquals(17, partition);
        }
    }


    @Test
    public void convertTimeTest() throws ParseException {
        String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
        DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT).withZone(ZoneId.systemDefault());

        String startTime = "2024-04-01 00:00:00.000";
        String endTime = "2024-04-24 00:00:00.000";

        logger.debug("start time value : {}", SIMPLE_DATE_FORMATTER.parse(startTime, Instant::from).toEpochMilli());
        logger.debug("end time value : {}", SIMPLE_DATE_FORMATTER.parse(endTime, Instant::from).toEpochMilli());
    }

}
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

package com.navercorp.pinpoint.common.dao.pinot;

import org.apache.kafka.clients.producer.internals.BuiltInPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author minwoo-jung
 */


@Disabled
class AgentStatTopicAndTableNameManagerTest {

    private final Logger LOGGER = LogManager.getLogger(AgentStatTopicAndTableNameManagerTest.class.getName());
    @Test
    public void getAgentStatTopicName() {
        String applicationName = "pinpointApplication";
        int agentStatTopicCount = 16;
        String agentStatTopicName = AgentStatTopicAndTableNameManager.getAgentStatTopicName(applicationName, agentStatTopicCount);
        LOGGER.info(agentStatTopicName);
//        assertEquals("inspector-stat-agent-00", agentStatTopicName);
    }

    @Test
    public void kafkaPartitionForStringSortKeyTest() {
        int numPartitions = 32;
        StringSerializer keySerializer = new StringSerializer();
        byte[] keyBytes = keySerializer.serialize("inspector-stat", "pub-vpc-nas-api#avnas-mcweb01.ncp3#directBuffer");
        int partition = BuiltInPartitioner.partitionForKey(keyBytes, numPartitions);
        LOGGER.info(partition);
//        assertEquals(54, partition);
    }


    @Test
    public void convertTimeTest() throws ParseException {
        String startTime = "2024-04-01 00:00:00.000";
        String endTime = "2024-04-24 00:00:00.000";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Date startTimeDate = simpleDateFormat.parse(startTime);
        long startTimeInMillis = startTimeDate.getTime();
        LOGGER.info("start time value : " + startTimeInMillis);
        Date endTimeDate = simpleDateFormat.parse(endTime);
        long endTimeInMillis = endTimeDate.getTime();
        LOGGER.info("end time value : " + endTimeInMillis);
    }

}
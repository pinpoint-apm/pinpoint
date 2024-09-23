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

package com.navercorp.pinpoint.inspector.collector.dao.pinot;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.kafka.clients.producer.internals.BuiltInPartitioner;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pinot.segment.spi.partition.MurmurPartitionFunction;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author minwoo-jung
 */
class DefaultAgentStatDaoTest {

    private final Logger logger = LogManager.getLogger(DefaultAgentStatDaoTest.class.getName());
    @Test
    public void kafkaPatitionTest() {
        int numPartitions = 64;
        String key = "minwoo_local_app#jvmGc";
        int partition = Utils.toPositive(Utils.murmur2(key.getBytes())) % numPartitions;
        assertEquals(18, partition);
    }

    @Test
    public void kafkaPatitionTest2() {
        int numPartitions = 128;
        String key = "minwoo_local_app2#minwoo_local_agent2#dataSource";
        int partition = Utils.toPositive(Utils.murmur2(key.getBytes())) % numPartitions;
        assertEquals(118, partition);
    }

//    @Test
    public void googleHashFunctionTest() {
        HashFunction hashFunction = Hashing.murmur3_128();
        String sortKey = "applicationName#agentId#metricName";
        byte[] bytes = hashFunction.hashString(sortKey, StandardCharsets.UTF_8).asBytes();
        logger.info(Arrays.toString(bytes));
    }


    @Test
    public void googleHashFunctionTest2() {
        HashFunction hashFunction = Hashing.murmur3_128();
        String sortKey = "applicationName#agentId#metricName";
        Long longValue = hashFunction.hashString(sortKey, StandardCharsets.UTF_8).asLong();
        logger.info(longValue);

        String sortKey2 = "applicationName#agentId#metricName";
        Long longValue2 = hashFunction.hashString(sortKey2, StandardCharsets.UTF_8).asLong();
        logger.info(longValue2);

        assertEquals(longValue, longValue2);


        String sortKey3 = "applicationName2#agentId2#metricName2";
        Long longValue3 = hashFunction.hashString(sortKey3, StandardCharsets.UTF_8).asLong();
        logger.info(longValue3);

        String sortKey4 = "applicationName3#agentId3#metricName3";
        Long longValue4 = hashFunction.hashString(sortKey4, StandardCharsets.UTF_8).asLong();
        logger.info(longValue4);

        assertNotEquals(longValue3, longValue4);

    }

    @Test
    public void kafkaPartitionForStringSortKeyTest() {
        int numPartitions = 64;
        StringSerializer keySerializer = new StringSerializer();
        byte[] keyBytes = keySerializer.serialize("inspector-stat", "minwoo_local_app2#minwoo_local_agent2#dataSource");
        int partition = BuiltInPartitioner.partitionForKey(keyBytes, numPartitions);
        assertEquals(54, partition);
    }


    //kafka partition 결과 int sortKey
    @Test
    public void kafkaPartitionForLongSortKeyTest() {
        int numPartitions = 128;
        Long longValue = -4545381519295174261L;

        LongSerializer keySerializer = new LongSerializer();
        byte[] keyBytes = keySerializer.serialize("inspector-stat", longValue);
        int partition = BuiltInPartitioner.partitionForKey(keyBytes, numPartitions);
        assertEquals(45, partition);
    }

    @Test
    public void pinotPartitionForLongSortKeyTest() {
        int numPartitions = 128;
        Long longValue = -4545381519295174261L;
        MurmurPartitionFunction murmurPartitionFunction = new MurmurPartitionFunction(numPartitions);
        int partition = murmurPartitionFunction.getPartition(longValue.toString());
        assertEquals(105, partition);
    }

    @Test
    public void comparePartitionOperationInKafkaAndPinotForLongSortKeyTest() {
        int numPartitions = 128;
        Long longValue = 5522573437844253163L;
        MurmurPartitionFunction murmurPartitionFunction = new MurmurPartitionFunction(numPartitions);
        int pinotPartition = murmurPartitionFunction.getPartition(longValue.toString());
        assertEquals(1, pinotPartition);

        LongSerializer keySerializer = new LongSerializer();
        byte[] keyBytes = longValue.toString().getBytes(StandardCharsets.UTF_8);
        int kafkaPartition = BuiltInPartitioner.partitionForKey(keyBytes, numPartitions);
        assertEquals(1, kafkaPartition);
    }

    @Test
    public void compareConvetedByteArray() {
        Long longValue = -4545381519295174261L;

        LongSerializer keySerializer = new LongSerializer();
        byte[] keyBytesByKafka = keySerializer.serialize("inspector-stat", longValue);

        byte[] keyBytesByPinot = longValue.toString().getBytes(StandardCharsets.UTF_8);

        logger.info(longValue.toString());
        logger.info(Arrays.toString(keyBytesByKafka));
        logger.info(Arrays.toString(keyBytesByPinot));
    }



}
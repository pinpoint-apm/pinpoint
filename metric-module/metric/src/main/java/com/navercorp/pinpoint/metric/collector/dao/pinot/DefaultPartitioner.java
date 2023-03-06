/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.metric.collector.dao.pinot;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.StickyPartitionCache;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.utils.Utils;

import java.util.Map;

/**
 * @author minwoo.jung
 */
public class DefaultPartitioner implements Partitioner {
    private final StickyPartitionCache stickyPartitionCache = new StickyPartitionCache();

    public DefaultPartitioner() {
    }

    public void configure(Map<String, ?> configs) {
    }

    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        return this.partition(topic, key, keyBytes, value, valueBytes, cluster, cluster.partitionsForTopic(topic).size());
    }

    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster, int numPartitions) {
        int partitionNumber = keyBytes == null ? this.stickyPartitionCache.partition(topic, cluster) : Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
        System.out.println("$$$ partition Number : " + key + " " + partitionNumber);
        return partitionNumber;
    }

    public void close() {
    }

    public void onNewBatch(String topic, Cluster cluster, int prevPartition) {
        this.stickyPartitionCache.nextPartition(topic, cluster, prevPartition);
    }
}
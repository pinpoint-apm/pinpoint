/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase.it;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@SpringJUnitConfig(HbaseTestClusterConfiguration.class)
class HbaseClusterIT {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final TableName TABLE = TableName.valueOf("mini_cluster_it");
    private static final byte[] CF = Bytes.toBytes("cf");

    @Autowired
    private HbaseTestCluster cluster;

    @Autowired
    @Qualifier("hbaseConnection")
    private Connection connection;

    @BeforeEach
    void createTable() throws Exception {
        cluster.createTable(TABLE, CF);
    }

    @Test
    void putAndGet() throws Exception {
        logger.info("putAndGet start");
        try (Table table = connection.getTable(TABLE)) {
            Put put = new Put(Bytes.toBytes("row1"));
            put.addColumn(CF, Bytes.toBytes("name"), Bytes.toBytes("alice"));
            table.put(put);

            Result result = table.get(new Get(Bytes.toBytes("row1")));
            byte[] value = result.getValue(CF, Bytes.toBytes("name"));
            assertEquals("alice", Bytes.toString(value));
        }
    }
}

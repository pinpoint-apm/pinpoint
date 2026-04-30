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

import jakarta.annotation.PostConstruct;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.testing.TestingHBaseCluster;
import org.apache.hadoop.hbase.testing.TestingHBaseClusterOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class HbaseTestCluster implements AutoCloseable {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TestingHBaseCluster cluster;

    public HbaseTestCluster() {
        this(defaultOption());
    }

    public HbaseTestCluster(TestingHBaseClusterOption option) {
        Objects.requireNonNull(option, "option");
        this.cluster = TestingHBaseCluster.create(option);
    }

    public static TestingHBaseClusterOption defaultOption() {
//        Configuration conf = HBaseConfiguration.create();
//        // AsyncFSWAL fails with NPE on JDK 17+ MiniDFSCluster; force synchronous FSHLog for tests.
//        conf.set("hbase.wal.provider", "filesystem");
//        conf.set("hbase.wal.meta_provider", "filesystem");
//        conf.setBoolean("hbase.unsafe.stream.capability.enforce", false);
//
//        // MiniDFSCluster has a single DataNode. Prevent FSHLog sync failures
//        // (DamagedWALException) that would otherwise abort the master.
//        conf.setInt("dfs.replication", 1);
//        conf.setInt("hbase.regionserver.hlog.replication", 1);
//        conf.setInt("hbase.regionserver.hlog.tolerable.lowreplication", 1);
//        conf.setBoolean("dfs.client.block.write.replace-datanode-on-failure.enable", false);

//        return TestingHBaseClusterOption.builder()
//                .conf(conf)
//                .build();

        TestingHBaseClusterOption.Builder builder = TestingHBaseClusterOption.builder();
//        builder.numDataNodes(1);
//        builder.numRegionServers(1);
//        builder.numZkServers(1);
        return builder
                .build();
    }

    @PostConstruct
    public void start() {
        logger.info("TestingHBaseCluster start");
        try {
            cluster.start();
        } catch (Exception e) {
            throw new HbaseTestClusterException(e);
        }
    }

    @Override
    public void close() {
        logger.info("TestingHBaseCluster close");
        try {
//            disableAndDeleteAllTables();
            cluster.stop();
        } catch (Exception e) {
            throw new HbaseTestClusterException(e);
        }
    }

    public boolean isHBaseClusterRunning() {
        return cluster.isHBaseClusterRunning();
    }

    public Configuration getConfiguration() {
        return cluster.getConf();
    }

    private void disableAndDeleteAllTables() {
        try (Connection connection = ConnectionFactory.createConnection(cluster.getConf());
             Admin admin = connection.getAdmin()) {
            for (TableName tableName : admin.listTableNames()) {
                if (admin.isTableEnabled(tableName)) {
                    admin.disableTable(tableName);
                }
                admin.deleteTable(tableName);
            }
        } catch (Exception e) {
            logger.info("disableAndDeleteAllTables failed", e);
        }
    }

    public void createTable(TableName tableName, byte[] family) throws Exception {
        try (Connection connection = ConnectionFactory.createConnection(cluster.getConf());
             Admin admin = connection.getAdmin()) {
            if (admin.tableExists(tableName)) {
                if (admin.isTableEnabled(tableName)) {
                    admin.disableTable(tableName);
                }
                admin.deleteTable(tableName);
            }
            TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName)
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.of(family));
            admin.createTable(builder.build());
        }
    }
}
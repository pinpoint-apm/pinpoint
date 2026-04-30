package com.navercorp.pinpoint.common.hbase.test;

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class HbaseDaoTest {

    private static final HBaseTestingUtility UTIL = new HBaseTestingUtility();
    private static final TableName TABLE = TableName.valueOf("my_test");
    private static final byte[] CF = Bytes.toBytes("cf");

    private static Connection connection;

    @BeforeAll
    static void startCluster() throws Exception {
        UTIL.startMiniCluster();
        connection = ConnectionFactory.createConnection(UTIL.getConfiguration());
    }

    @AfterAll
    static void stopCluster() throws Exception {
        if (connection != null) {
            connection.close();
        }
        UTIL.shutdownMiniCluster();
    }

    @BeforeEach
    void createTable() throws Exception {
        if (UTIL.getAdmin().tableExists(TABLE)) {
            UTIL.deleteTable(TABLE);
        }
        UTIL.createTable(TABLE, CF);
    }

    @Test
    void putAndGet() throws Exception {
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

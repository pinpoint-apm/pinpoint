package com.navercorp.pinpoint.plugin.hbase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HbasePluginConstantsTest {

    @Test
    public void test() {
        assertEquals(HbasePluginConstants.HBASE_CLIENT.getCode(), 8800);
        assertEquals(HbasePluginConstants.HBASE_CLIENT_ADMIN.getCode(), 8801);
        assertEquals(HbasePluginConstants.HBASE_CLIENT_TABLE.getCode(), 8802);
        assertEquals(HbasePluginConstants.HBASE_ASYNC_CLIENT.getCode(), 8803);
        assertEquals(HbasePluginConstants.HBASE_CLIENT_PARAMS.getCode(), 320);
        assertEquals(HbasePluginConstants.HBASE_CLIENT_SCOPE, "HBASE_CLIENT_SCOPE");
        assertEquals(HbasePluginConstants.HBASE_DESTINATION_ID, "HBASE");
        assertEquals(HbasePluginConstants.HBASE_CLIENT_CONFIG, "profiler.hbase.client.enable");
        assertEquals(HbasePluginConstants.HBASE_CLIENT_ADMIN_CONFIG, "profiler.hbase.client.admin.enable");
        assertEquals(HbasePluginConstants.HBASE_CLIENT_TABLE_CONFIG, "profiler.hbase.client.table.enable");
        assertEquals(HbasePluginConstants.HBASE_CLIENT_PARAMS_CONFIG, "profiler.hbase.client.params.enable");
    }
}
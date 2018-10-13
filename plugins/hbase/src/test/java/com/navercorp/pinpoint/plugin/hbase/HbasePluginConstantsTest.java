package com.navercorp.pinpoint.plugin.hbase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HbasePluginConstantsTest {

    @Test
    public void test() {
        assertEquals(HbasePluginConstants.HBASE.getCode(), 8800);
        assertEquals(HbasePluginConstants.HBASE_ADMIN.getCode(), 8801);
        assertEquals(HbasePluginConstants.HBASE_TABLE.getCode(), 8802);
        assertEquals(HbasePluginConstants.HBASE_PARAMS.getCode(), 320);
        assertEquals(HbasePluginConstants.HBASE_SCOPE, "HBASE_SCOPE");
        assertEquals(HbasePluginConstants.HBASE_CONFIG, "profiler.hbase.enable");
        assertEquals(HbasePluginConstants.HBASE_OPS_CONFIG, "profiler.hbase.operation.enable");
    }
}
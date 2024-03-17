package com.navercorp.pinpoint.plugin.hbase;

public final class HbaseVersion {
    public static final int HBASE_VERSION_UNKNOWN = -1;
    public static final int HBASE_VERSION_0 = 0_00_00;
    public static final int HBASE_VERSION_1 = 1_00_00;

    static final String HBASE0_TABLE_INTERFACE_NAME = "org.apache.hadoop.hbase.client.HTableInterface";
    static final String HBASE1_TABLE_INTERFACE_NAME = "org.apache.hadoop.hbase.client.Table";

    public static int getVersion(ClassLoader cl) {
        // hbase 1.x+
        final Class<?> hbase1Table = ReflectionUtils.getClass(cl, HBASE1_TABLE_INTERFACE_NAME);
        if (hbase1Table != null) {
            return HbaseVersion.HBASE_VERSION_1;
        }
        // hbase 0.x-
        final Class<?> hbase0Table = ReflectionUtils.getClass(cl, HBASE0_TABLE_INTERFACE_NAME);
        if (hbase0Table != null) {
            return HbaseVersion.HBASE_VERSION_0;
        }
        return HbaseVersion.HBASE_VERSION_UNKNOWN;
    }
}

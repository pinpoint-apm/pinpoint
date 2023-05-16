package com.navercorp.pinpoint.plugin.hbase.interceptor.util;

import com.navercorp.pinpoint.plugin.hbase.HbaseVersion;

public final class HbaseTableNameProviderFactory {

    private HbaseTableNameProviderFactory() {
    }

    public static HbaseTableNameProvider getTableNameProvider(int version) {
        switch (version) {
            case HbaseVersion.HBASE_VERSION_0:
                return new Hbase0TableNameProvider();
            case HbaseVersion.HBASE_VERSION_1:
                return new Hbase1TableNameProvider();
            default:
                return new UnsupportedGetNameProvider();
        }
    }

}

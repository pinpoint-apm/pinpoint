package com.navercorp.pinpoint.plugin.hbase.interceptor.util;

import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;

public final class UnsupportedGetNameProvider implements HbaseTableNameProvider {

    @Override
    public String getName(Object target) {
        return HbasePluginConstants.UNKNOWN_TABLE;
    }
}

package com.navercorp.pinpoint.plugin.hbase.interceptor.util;

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.apache.hadoop.hbase.client.HTableInterface;

public final class Hbase0TableNameProvider implements HbaseTableNameProvider {
    @Override
    @SuppressWarnings("deprecation")
    public String getName(Object target) {
        if (target instanceof HTableInterface) {
            final HTableInterface hTable = (HTableInterface) target;
            return getNameAsString(hTable);
        }
        return HbasePluginConstants.UNKNOWN_TABLE;
    }

    @SuppressWarnings("deprecation")
    private String getNameAsString(HTableInterface table) {
        byte[] tableName = table.getTableName();
        return BytesUtils.toString(tableName);
    }

}

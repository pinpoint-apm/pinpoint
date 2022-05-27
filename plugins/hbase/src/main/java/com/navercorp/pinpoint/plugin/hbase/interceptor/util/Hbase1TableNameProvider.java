package com.navercorp.pinpoint.plugin.hbase.interceptor.util;

import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Table;

public final class Hbase1TableNameProvider implements HbaseTableNameProvider {
    @Override
    public String getName(Object target) {
        if (target instanceof Table) {
            final Table table = (Table) target;
            return getNameAsString(table);
        }

        return HbasePluginConstants.UNKNOWN_TABLE;
    }

    private String getNameAsString(Table table) {
        TableName tableName = table.getName();
        return tableName.getNameAsString();
    }

}

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Put;

import java.util.List;

/**
 * @Author Taejin Koo
 */
public interface HBaseAsyncOperation {

    boolean put(String tableName, final Put put);

    List<Put> put(String tableName, final List<Put> puts);

}
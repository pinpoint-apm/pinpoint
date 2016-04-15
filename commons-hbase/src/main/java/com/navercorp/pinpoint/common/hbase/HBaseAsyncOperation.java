package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;

/**
 * @Author Taejin Koo
 */
public interface HBaseAsyncOperation {

    boolean isAvailable();

    boolean put(TableName tableName, final Put put);

    List<Put> put(TableName tableName, final List<Put> puts);

    Long getCurrentPutOpsCount();

}
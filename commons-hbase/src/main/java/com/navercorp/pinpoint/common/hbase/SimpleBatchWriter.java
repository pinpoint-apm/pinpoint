package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;


public interface SimpleBatchWriter {

    boolean write(TableName tableName, Put mutation);
}

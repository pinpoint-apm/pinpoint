package com.navercorp.pinpoint.common.hbase.batch;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Mutation;

import java.util.List;

public interface HbaseBatchWriter {
    boolean write(TableName tableName, List<? extends Mutation> mutations);

    boolean write(TableName tableName, Mutation mutation);
}

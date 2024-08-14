package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AsyncBufferedMutator;

import java.util.concurrent.ExecutorService;

public interface AsyncBufferedMutatorFactory {

    AsyncBufferedMutator getBufferedMutator(TableName tableName, ExecutorService pool);

    AsyncBufferedMutator getBufferedMutator(TableName tableName);
}

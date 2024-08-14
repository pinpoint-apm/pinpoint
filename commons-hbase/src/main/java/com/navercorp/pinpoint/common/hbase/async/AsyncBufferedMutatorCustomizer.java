package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.client.AsyncBufferedMutatorBuilder;

public interface AsyncBufferedMutatorCustomizer {
    void customize(AsyncBufferedMutatorBuilder builder);
}

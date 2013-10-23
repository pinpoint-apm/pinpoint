package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Result;

/**
 *
 */
public interface LimitEventHandler {
    void handleLastResult(Result lastResult);
}

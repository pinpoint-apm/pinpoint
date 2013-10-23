package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Result;

/**
 *
 */
public class EmptyLimitEventHandler implements LimitEventHandler{

    @Override
    public void handleLastResult(Result lastResult) {
    }
}

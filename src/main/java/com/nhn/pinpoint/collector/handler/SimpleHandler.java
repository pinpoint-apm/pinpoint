package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;

/**
 *
 */
public interface SimpleHandler {
    void handler(TBase<?, ?> tbase);
}

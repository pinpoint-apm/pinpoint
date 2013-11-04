package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;

/**
 * @author emeroad
 */
public interface SimpleHandler {
    void handler(TBase<?, ?> tbase);
}

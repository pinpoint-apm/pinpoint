package com.nhn.pinpoint.profiler.context;

import org.apache.thrift.TBase;

/**
 *
 */
public interface Thriftable {
    TBase toThrift();
}

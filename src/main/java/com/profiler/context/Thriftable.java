package com.profiler.context;

import org.apache.thrift.TBase;

/**
 *
 */
public interface Thriftable {
    TBase toThrift();
}

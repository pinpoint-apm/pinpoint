package com.nhn.pinpoint.context;

import org.apache.thrift.TBase;

/**
 *
 */
public interface Thriftable {
    TBase toThrift();
}

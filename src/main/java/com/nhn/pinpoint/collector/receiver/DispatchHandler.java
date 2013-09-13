package com.nhn.pinpoint.collector.receiver;

import org.apache.thrift.TBase;

/**
 *
 */
public interface DispatchHandler {
    TBase dispatch(TBase<?, ?> tBase, byte[] packet, int offset, int length);

}

package com.nhn.pinpoint.collector.receiver;

import org.apache.thrift.TBase;

/**
 * @author emeroad
 */
public interface DispatchHandler {
    TBase dispatch(TBase<?, ?> tBase, byte[] packet, int offset, int length);

}

package com.nhn.pinpoint.profiler.sender;

import org.apache.thrift.TBase;

import com.nhn.pinpoint.rpc.FutureListener;
import com.nhn.pinpoint.rpc.client.PinpointSocketReconnectEventListener;

/**
 * @author emeroad
 */
public interface EnhancedDataSender extends DataSender {

    boolean request(TBase<?, ?> data);
    boolean request(TBase<?, ?> data, int retry);
    boolean request(TBase<?, ?> data, FutureListener listener);

    boolean addReconnectEventListener(PinpointSocketReconnectEventListener eventListener);
    boolean removeReconnectEventListener(PinpointSocketReconnectEventListener eventListener);

}

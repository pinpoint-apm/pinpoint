package com.nhn.pinpoint.profiler.sender;

import org.apache.thrift.TBase;

/**
 * @author emeroad
 * @author netspider
 */
public interface DataSender {

    boolean send(TBase<?, ?> data);

    void stop();

    boolean isNetworkAvailable();
}

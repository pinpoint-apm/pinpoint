package com.profiler.sender;

import org.apache.thrift.TBase;

/**
 *
 */
public interface DataSender {

    boolean send(TBase<?, ?> data);

    void stop();

}

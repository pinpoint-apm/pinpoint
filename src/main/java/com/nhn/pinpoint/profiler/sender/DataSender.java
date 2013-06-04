package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.profiler.context.Thriftable;
import org.apache.thrift.TBase;

/**
 *
 */
public interface DataSender {

    boolean send(TBase<?, ?> data);

    boolean send(Thriftable thriftable);

    void stop();

}

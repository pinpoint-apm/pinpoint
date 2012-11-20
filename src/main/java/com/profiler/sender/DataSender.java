package com.profiler.sender;

import com.profiler.context.Thriftable;
import org.apache.thrift.TBase;

/**
 *
 */
public interface DataSender {

    boolean send(TBase<?, ?> data);

    boolean send(Thriftable thriftable);

    void stop();

}

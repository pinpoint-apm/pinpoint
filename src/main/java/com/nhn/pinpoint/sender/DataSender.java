package com.nhn.pinpoint.sender;

import com.nhn.pinpoint.context.Thriftable;
import org.apache.thrift.TBase;

/**
 *
 */
public interface DataSender {

    boolean send(TBase<?, ?> data);

    boolean send(Thriftable thriftable);

    void stop();

}

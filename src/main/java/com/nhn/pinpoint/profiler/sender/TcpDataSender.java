package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.common.rpc.client.PinpointSocket;
import com.nhn.pinpoint.common.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.profiler.context.Thriftable;
import org.apache.thrift.TBase;

/**
 *
 */
public class TcpDataSender implements DataSender {

    private PinpointSocketFactory pinpointSocketFactory;

    public TcpDataSender(String host, int port) {
        pinpointSocketFactory = new PinpointSocketFactory();
        PinpointSocket socket = pinpointSocketFactory.connect(host, port);
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        return false;
    }

    @Override
    public boolean send(Thriftable thriftable) {
        return false;
    }

    @Override
    public void stop() {
        pinpointSocketFactory.release();
    }
}

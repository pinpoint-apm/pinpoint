package com.navercorp.pinpoint.rpc;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * @Author Taejin Koo
 */
public interface PinpointDatagramSocket {

    void connect(SocketAddress address);

    boolean isConnected();

    void send(byte[] buf) throws IOException;
    void send(byte[] buf, int offset, int length);

    void send(ByteBuffer byteBuf);

    void receive(byte[] buf);
    void receive(ByteBuffer byteBuf);

    void close();

}

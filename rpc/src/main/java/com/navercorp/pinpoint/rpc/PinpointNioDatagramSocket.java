package com.navercorp.pinpoint.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @Author Taejin Koo
 */
public class PinpointNioDatagramSocket implements PinpointDatagramSocket {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DatagramChannel datagramChannel;
    private SocketAddress remoteAddress;

    public PinpointNioDatagramSocket(int timeout, int sendBufferSize) {
        DatagramChannel datagramChannel = createDatagramChannel(timeout, sendBufferSize);
        this.datagramChannel = datagramChannel;
    }


    private DatagramChannel createDatagramChannel(int timeout, int sendBufferSize) {
        DatagramChannel datagramChannel = null;
        DatagramSocket socket = null;
        try {
            datagramChannel = DatagramChannel.open();
            socket = datagramChannel.socket();
            socket.setSoTimeout(timeout);
            socket.setSendBufferSize(sendBufferSize);

            if (logger.isWarnEnabled()) {
                final int checkSendBufferSize = socket.getSendBufferSize();
                if (sendBufferSize != checkSendBufferSize) {
                    logger.warn("DatagramChannel.setSendBufferSize() error. {}!={}", sendBufferSize, checkSendBufferSize);
                }
            }
            return datagramChannel;
        } catch (IOException e) {
            throw new IllegalStateException("DatagramChannel create fail. Cause" + e.getMessage(), e);
        }
    }

    @Override
    public void connect(SocketAddress address) {
        try {
            datagramChannel.connect(address);
            remoteAddress = address;
        } catch (IOException e) {
            DatagramSocket socket = datagramChannel.socket();
            if (socket != null) {
                socket.close();
            }

            if (datagramChannel != null) {
                try {
                    datagramChannel.close();
                } catch (IOException e1) {
                    // ignore
                }
            }

            throw new PinpointSocketException("DatagramChannel create fail. Caused : " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return datagramChannel.isConnected();
    }

    @Override
    public void send(byte[] buf) {
        send(ByteBuffer.wrap(buf));
    }

    @Override
    public void send(byte[] buf, int offset, int length) {
        send(ByteBuffer.wrap(buf, offset, length));
    }

    @Override
    public void send(ByteBuffer byteBuffer) {
        try {
            datagramChannel.write(byteBuffer);
        } catch (IOException e) {
            throw new PinpointSocketException(e.getMessage(), e);
        }
    }

    @Override
    public void receive(byte[] buf) {
        receive(ByteBuffer.wrap(buf));
    }

    @Override
    public void receive(ByteBuffer byteBuf) {
        try {
            datagramChannel.read(byteBuf);
        } catch (IOException e) {
            throw new PinpointSocketException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        DatagramSocket socket = datagramChannel.socket();
        if (socket != null) {
            socket.disconnect();
            socket.close();
        }

        try {
            datagramChannel.disconnect();
        } catch (IOException e) {
            // ignore
        }

        try {
            datagramChannel.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public String toString() {
        return "PinpointNioDatagramSocket{ =>" + remoteAddress + "}";
    }

}

package com.navercorp.pinpoint.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * @Author Taejin Koo
 */
public class PinpointOioDatagramSocket implements PinpointDatagramSocket {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DatagramSocket datagramSocket;
    private SocketAddress remoteSocketAddress;

    // Caution. not thread safe
    private final DatagramPacket reusePacket = new DatagramPacket(new byte[0], 0);

    public PinpointOioDatagramSocket(int timeout, int sendBufferSize) {
        DatagramSocket datagramSocket = createDatagramSocket(timeout, sendBufferSize);
        this.datagramSocket = datagramSocket;
    }

    private DatagramSocket createDatagramSocket(int timeout, int sendBufferSize) {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket();

            datagramSocket.setSoTimeout(timeout);
            datagramSocket.setSendBufferSize(sendBufferSize);
            if (logger.isInfoEnabled()) {
                final int checkSendBufferSize = datagramSocket.getSendBufferSize();
                if (sendBufferSize != checkSendBufferSize) {
                    logger.info("DatagramSocket.setSendBufferSize() error. {}!={}", sendBufferSize, checkSendBufferSize);
                }
            }

            return datagramSocket;
        } catch (SocketException e) {
            throw new IllegalStateException("DatagramSocket create fail. Cause" + e.getMessage(), e);
        }
    }

    @Override
    public void connect(SocketAddress address) {
        try {
            datagramSocket.connect(address);
            remoteSocketAddress = address;
        } catch (SocketException e) {
            throw new PinpointSocketException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return datagramSocket.isConnected();
    }

    @Override
    public void send(byte[] buf) throws IOException {
        reusePacket.setData(buf);
        send0(reusePacket);
    }

    @Override
    public void send(byte[] buf, int offset, int length) {
//        DatagramPacket datagramPacket = new DatagramPacket(buf, offset, length);
        reusePacket.setData(buf, offset, length);
        send0(reusePacket);
    }

    @Override
    public void send(ByteBuffer byteBuffer) {
        int byteBufferArrayOffset = byteBuffer.arrayOffset() + byteBuffer.position();
        int length = byteBuffer.remaining();

        reusePacket.setData(byteBuffer.array(), byteBufferArrayOffset, length);
        send0(reusePacket);
    }

    private void send0(DatagramPacket datagramPacket) {
        if (datagramPacket == null) {
            throw new NullPointerException("datagramPacket");
        }

        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            throw new PinpointSocketException(e.getMessage(), e);
        }
    }

    @Override
    public void receive(byte[] buf) {
        try {
            reusePacket.setData(buf);
            datagramSocket.receive(reusePacket);
        } catch (IOException e) {
            throw new PinpointSocketException(e.getMessage(), e);
        }
    }

    @Override
    public void receive(ByteBuffer byteBuf) {
        int remaining = byteBuf.remaining();
        byte[] buf = new byte[remaining];

        try {
            final DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
            datagramSocket.receive(receivePacket);
        } catch (IOException e) {
            throw new PinpointSocketException(e.getMessage(), e);
        }

        byteBuf.put(buf);
    }

    @Override
    public void close() {
        datagramSocket.disconnect();
        datagramSocket.close();
    }

    @Override
    public String toString() {
        return "PinpointOioDatagramSocket{ =>" + remoteSocketAddress + "}";
    }

}

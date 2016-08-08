package com.navercorp.pinpoint.profiler.sender;

/**
 * @Author Taejin Koo
 */
public final class UdpDataSenderFactory {

//    String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize

    private final String host;
    private final int port;
    private final String threadName;
    private final int queueSize;
    private final int timeout;
    private final int sendBufferSize;

    public UdpDataSenderFactory(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize) {
        this.host = host;
        this.port = port;
        this.threadName = threadName;
        this.queueSize = queueSize;
        this.timeout = timeout;
        this.sendBufferSize = sendBufferSize;
    }

    public DataSender create(String typeName) {
        return create(UdpDataSenderType.valueOf(typeName));
    }

    public DataSender create(UdpDataSenderType type) {
        if (type == UdpDataSenderType.NIO) {
            return new NioUDPDataSender(host, port, threadName, queueSize, timeout, sendBufferSize);
        } else if (type == UdpDataSenderType.OIO) {
            return new UdpDataSender(host, port, threadName, queueSize, timeout, sendBufferSize);
        } else {
            throw new IllegalArgumentException("Unknown type.");
        }
    }

}

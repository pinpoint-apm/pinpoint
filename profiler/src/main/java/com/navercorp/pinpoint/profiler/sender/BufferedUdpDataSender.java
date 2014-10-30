package com.nhn.pinpoint.profiler.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.thrift.io.ChunkHeaderBufferedTBaseSerializer;
import com.nhn.pinpoint.thrift.io.ChunkHeaderBufferedTBaseSerializerFlushHandler;

public class BufferedUdpDataSender extends UdpDataSender {
    private static final int BUFFER_SIZE = 1024 * 16;
    private static final String SCHEDULED_FLUSH = "ScheduledFlush";

    private final ChunkHeaderBufferedTBaseSerializer chunkSerializedBuffer;

    public BufferedUdpDataSender(String host, int port, String threadName, int queueSize) {
        this(host, port, threadName, queueSize, SOCKET_TIMEOUT, SEND_BUFFER_SIZE);
    }

    public BufferedUdpDataSender(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize) {
        super(host, port, threadName, queueSize, timeout, sendBufferSize);

        chunkSerializedBuffer = new ChunkHeaderBufferedTBaseSerializer(BUFFER_SIZE);
        chunkSerializedBuffer.setFlushHandler(new ChunkHeaderBufferedTBaseSerializerFlushHandler() {
            @Override
            public void handle(byte[] buffer, int offset, int length) {
                if (buffer == null) {
                    logger.warn("interBufferData is null");
                    return;
                }

                final int internalBufferSize = length;
                if (isLimit(internalBufferSize)) {
                    // udp 데이터 제한일 경우 error을 socket레벨에서 내는것보다는 먼저 체크하여 discard하는게 더 바람직함.
                    logger.warn("discard packet. Caused:too large message. size:{}", internalBufferSize);
                    return;
                }
                // single thread이므로 그냥 재활용한다.
                reusePacket.setData(buffer, 0, internalBufferSize);

                try {
                    udpSocket.send(reusePacket);
                    logger.debug("Data sent. {size={}, dump={}}", internalBufferSize, toStringBinary(buffer, offset, length));
                } catch (IOException e) {
                    logger.warn("packet send error. size:{}", internalBufferSize, e);
                }
            }
        });

        startScheduledFlush();
    }

    public static String toStringBinary(final byte[] b, int off, int len) {
        StringBuilder result = new StringBuilder();
        for (int i = off; i < off + len; ++i) {
            int ch = b[i] & 0xFF;
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || " `~!@#$%^&*()-_=+[]{}|;:'\",.<>/?".indexOf(ch) >= 0) {
                result.append((char) ch);
            } else {
                result.append(String.format("\\x%02X", ch));
            }
        }
        return result.toString();
    }

    private void startScheduledFlush() {
        final ThreadFactory threadFactory = new PinpointThreadFactory(SCHEDULED_FLUSH, true);
        final Thread thread = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    chunkSerializedBuffer.flush();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
        thread.start();
    }

    protected void sendPacket(Object message) {
        if (message instanceof TBase) {
            try {
                chunkSerializedBuffer.add((TBase<?, ?>) message);
                logger.debug("Send packet {message={}}", message);
            } catch (TException e) {
                logger.warn("sendPacket fail.", e);
            }
        } else {
            logger.warn("sendPacket fail. invalid type:{}", message != null ? message.getClass() : null);
            return;
        }
    }
}
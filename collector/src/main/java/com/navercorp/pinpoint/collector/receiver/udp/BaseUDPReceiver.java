package com.nhn.pinpoint.collector.receiver.udp;

import com.codahale.metrics.Timer;
import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import com.nhn.pinpoint.collector.util.PacketUtils;
import com.nhn.pinpoint.thrift.io.*;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.IOException;
import java.net.*;

/**
 * @author emeroad
 * @author netspider
 */
public class BaseUDPReceiver extends AbstractUDPReceiver {
    private DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory = new ThreadLocalHeaderTBaseDeserializerFactory<HeaderTBaseDeserializer>(new HeaderTBaseDeserializerFactory());

    public BaseUDPReceiver(String receiverName, DispatchHandler dispatchHandler, String bindAddress, int port, int receiverBufferSize, int workerThreadSize, int workerThreadQueueSize) {
        super(receiverName, dispatchHandler, bindAddress, port, receiverBufferSize, workerThreadSize, workerThreadQueueSize);
    }
    
    @Override
    Runnable getPacketDispatcher(AbstractUDPReceiver receiver, DatagramPacket packet) {
        return new DispatchPacket(receiver, packet);
    }

    private class DispatchPacket implements Runnable {
        private final AbstractUDPReceiver receiver;
        private final DatagramPacket packet;

        private DispatchPacket(AbstractUDPReceiver receiver, DatagramPacket packet) {
            if (packet == null) {
                throw new NullPointerException("packet must not be null");
            }
            this.receiver = receiver;
            this.packet = packet;
        }

        @Override
        public void run() {
            Timer.Context time = receiver.getTimer().time();

            final HeaderTBaseDeserializer deserializer = (HeaderTBaseDeserializer) deserializerFactory.createDeserializer();
            TBase<?, ?> tBase = null;
            try {
                tBase = deserializer.deserialize(packet.getData());
                if (tBase instanceof L4Packet) {
                    // 동적으로 패스가 가능하도록 보완해야 될듯 하다.
                    if (logger.isDebugEnabled()) {
                        L4Packet packet = (L4Packet) tBase;
                        logger.debug("udp l4 packet {}", packet.getHeader());
                    }
                    return;
                }
                // Network port availability check packet
                if (tBase instanceof NetworkAvailabilityCheckPacket) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("received udp network availability check packet.");
                    }
                    responseOK();
                    return;
                }
                // dispatch는 비지니스 로직 실행을 의미.
                receiver.getDispatchHandler().dispatchSendMessage(tBase, packet.getData(), Header.HEADER_SIZE, packet.getLength());
            } catch (TException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("packet serialize error. SendSocketAddress:{} Cause:{}", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } catch (Exception e) {
                // 잘못된 header가 도착할 경우 발생하는 케이스가 있음.
                if (logger.isWarnEnabled()) {
                    logger.warn("Unexpected error. SendSocketAddress:{} Cause:{} tBase:{}", packet.getSocketAddress(), e.getMessage(), tBase, e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            } finally {
                receiver.getDatagramPacketPool().returnObject(packet);
                // exception 난 경우는 어떻게 해야 하지?
                time.stop();
            }
        }

        private void responseOK() {
            try {
                byte[] okBytes = NetworkAvailabilityCheckPacket.DATA_OK;
                DatagramPacket pongPacket = new DatagramPacket(okBytes, okBytes.length, packet.getSocketAddress());
                receiver.getSocket().send(pongPacket);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("pong error. SendSocketAddress:{} Cause:{}", packet.getSocketAddress(), e.getMessage(), e);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("packet dump hex:{}", PacketUtils.dumpDatagramPacket(packet));
                }
            }
        }
    }

}

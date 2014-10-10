package com.nhn.pinpoint.rpc.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.packet.ControlEnableWorkerConfirmPacket;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamPacket;

/**
 * @author emeroad
 */
public class SimpleLoggingServerMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final SimpleLoggingServerMessageListener LISTENER = new SimpleLoggingServerMessageListener();

    @Override
    public void handleSend(SendPacket sendPacket, SocketChannel channel) {
        logger.info("handlerSend {} {}", sendPacket, channel);
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
        logger.info("handlerRequest {} {}", requestPacket, channel);
    }


    @Override
    public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
        logger.info("handlerStream {} {}", streamChannel, streamChannel);
    }
    
    @Override
    public int handleEnableWorker(Map properties) {
        logger.info("handleEnableWorker {}", properties);
        return ControlEnableWorkerConfirmPacket.SUCCESS;
    }

}

package com.nhn.pinpoint.rpc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.packet.HandshakeResponseCode;
import com.nhn.pinpoint.rpc.packet.HandshakeResponseType;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;

/**
 * @author emeroad
 */
public class TestSeverMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private byte[] open;
    private List<byte[]> sendMessageList = new ArrayList<byte[]>();

    @Override
    public void handleSend(SendPacket sendPacket, SocketChannel channel) {
        logger.debug("sendPacket:{} channel:{}", sendPacket, channel);
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
        logger.debug("requestPacket:{} channel:{}", requestPacket, channel);

        channel.sendResponseMessage(requestPacket, requestPacket.getPayload());
    }

    @Override
    public HandshakeResponseCode handleHandshake(Map properties) {
        logger.debug("handle handShake properties:{} channel:{}", properties);
        return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
    }

    public byte[] getOpen() {
        return open;
    }

    public List<byte[]> getSendMessage() {
        return sendMessageList;
    }
}


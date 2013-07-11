package com.nhn.pinpoint.common.rpc.server;

import com.nhn.pinpoint.common.rpc.TestByteUtils;
import com.nhn.pinpoint.common.io.rpc.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
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

        channel.sendResponseMessage(requestPacket.getRequestId(), requestPacket.getPayload());
    }


    @Override
    public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
        logger.debug("streamPacket:{} channel:{}", streamPacket, streamChannel);
        if (streamPacket instanceof StreamCreatePacket) {
            byte[] payload = streamPacket.getPayload();
            this.open = payload;
            streamChannel.sendOpenResult(true, payload);
            sendStreamMessage(streamChannel);
            sendStreamMessage(streamChannel);
            sendStreamMessage(streamChannel);

        }  else if(streamPacket instanceof StreamClosePacket) {
            // 채널 종료해야 함.
        }

    }

    private void sendStreamMessage(ServerStreamChannel streamChannel) {
        byte[] randomByte = TestByteUtils.createRandomByte(10);
        streamChannel.sendStreamMessage(randomByte);
        sendMessageList.add(randomByte);
    }

    public byte[] getOpen() {
        return open;
    }

    public List<byte[]> getSendMessage() {
        return sendMessageList;
    }
}


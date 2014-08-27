package com.nhn.pinpoint.rpc.server;

import com.nhn.pinpoint.rpc.TestByteUtils;
import com.nhn.pinpoint.rpc.packet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
        logger.debug("streamPacket:{} channel:{}", streamPacket, streamChannel);
        if (streamPacket instanceof StreamCreatePacket) {
            byte[] payload = streamPacket.getPayload();
            this.open = payload;
            streamChannel.sendOpenResult(true, payload);
            sendStreamMessage(streamChannel);
            sendStreamMessage(streamChannel);
            sendStreamMessage(streamChannel);

            sendClose(streamChannel);
        }  else if(streamPacket instanceof StreamClosePacket) {
            // 채널 종료해야 함.
        }

    }
    
    @Override
    public int handleEnableWorker(Map properties) {
        logger.debug("handleEnableWorker properties:{} channel:{}", properties);
        return ControlEnableWorkerConfirmPacket.SUCCESS;
    }

    private void sendClose(ServerStreamChannel streamChannel) {
        sendMessageList.add(new byte[0]);
        streamChannel.close();
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


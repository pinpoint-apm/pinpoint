package com.nhn.pinpoint.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.client.StreamChannel;
import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.nhn.pinpoint.rpc.stream.StreamChannelMessageListener;

/**
 * @author emeroad
 * @author koo.taejin <kr14910>
 */
public class RecordedStreamChannelMessageListener implements StreamChannelMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CountDownLatch latch;

    private final List<byte[]> receivedMessageList = Collections.synchronizedList(new ArrayList<byte[]>());

    public RecordedStreamChannelMessageListener(int receiveMessageCount) {
        this.latch = new CountDownLatch(receiveMessageCount);
    }

	@Override
	public short handleStreamCreate(StreamChannel streamChannel, StreamCreatePacket packet) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void handleStreamData(StreamChannel streamChannel, StreamResponsePacket packet) {
        logger.info("handleStreamData {}, {}", streamChannel, packet);
        receivedMessageList.add(packet.getPayload());
        latch.countDown();
	}


	@Override
	public void handleStreamClose(StreamChannel streamChannel, StreamClosePacket packet) {
        logger.info("handleClose {}, {}", streamChannel, packet);
        receivedMessageList.add(packet.getPayload());
        latch.countDown();
	}

    public CountDownLatch getLatch() {
        return latch;
    }

    public List<byte[]> getReceivedMessage() {
        return receivedMessageList;
    }

}


package com.nhn.pinpoint.rpc;

import com.nhn.pinpoint.rpc.client.StreamChannel;
import com.nhn.pinpoint.rpc.client.StreamChannelMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author emeroad
 */
public class RecordedStreamChannelMessageListener implements StreamChannelMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CountDownLatch latch;

    private final List<byte[]> receivedMessageList = Collections.synchronizedList(new ArrayList<byte[]>());

    public RecordedStreamChannelMessageListener(int receiveMessageCount) {
        this.latch = new CountDownLatch(receiveMessageCount);
    }


    @Override
    public void handleStreamResponse(StreamChannel streamChannel, byte[] bytes) {
        logger.info("handleStreamResponse {}, {}", streamChannel, bytes.length);
        receivedMessageList.add(bytes);
        latch.countDown();
    }

    @Override
    public void handleClose(StreamChannel streamChannel, byte[] bytes) {
        logger.info("handleClose {}, {}", streamChannel, bytes.length);
        receivedMessageList.add(bytes);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public List<byte[]> getReceivedMessage() {
        return receivedMessageList;
    }
}


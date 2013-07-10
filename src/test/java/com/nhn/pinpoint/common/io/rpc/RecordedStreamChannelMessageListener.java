package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.client.StreamChannel;
import com.nhn.pinpoint.common.io.rpc.client.StreamChannelMessageListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class RecordedStreamChannelMessageListener implements StreamChannelMessageListener {

    private final CountDownLatch latch;

    private List<byte[]> receivedMessageList = Collections.synchronizedList(new ArrayList<byte[]>());

    public RecordedStreamChannelMessageListener(int receiveMessageCount) {
        this.latch = new CountDownLatch(receiveMessageCount);
    }


    @Override
    public void handleStream(StreamChannel streamChannel, byte[] bytes) {
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


/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class RecordedStreamChannelMessageListener extends ClientStreamChannelEventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CountDownLatch latch;

    private StreamChannelStateCode stateCode;
    private int stateUpdatedCount;

    private final List<byte[]> receivedMessageList = Collections.synchronizedList(new ArrayList<byte[]>());

    public RecordedStreamChannelMessageListener(int receiveMessageCount) {
        this.latch = new CountDownLatch(receiveMessageCount);
    }

    @Override
    public void handleStreamResponsePacket(ClientStreamChannel streamChannel, StreamResponsePacket packet) {
        logger.debug("handleStreamResponsePacket() {}, {}", streamChannel, packet);
        receivedMessageList.add(packet.getPayload());
        latch.countDown();
    }

    @Override
    public void handleStreamClosePacket(ClientStreamChannel streamChannel, StreamClosePacket packet) {
        logger.debug("handleStreamClosePacket() {}, {}", streamChannel, packet);
        receivedMessageList.add(packet.getPayload());
        latch.countDown();
    }

    @Override
    public void stateUpdated(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) {
        this.stateCode = updatedStateCode;
        this.stateUpdatedCount++;
    }

    public StreamChannelStateCode getCurrentState() {
        return stateCode;
    }

    public int getStateUpdatedCount() {
        return stateUpdatedCount;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public List<byte[]> getReceivedMessage() {
        return receivedMessageList;
    }

}


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

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPingPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPongPacket;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author koo.taejin
 */
public abstract class StreamChannel {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentHashMap<String, Object> attribute = new ConcurrentHashMap<String, Object>();

    protected final Channel channel;
    private final int streamChannelId;
    protected final StreamChannelState state  = new StreamChannelState();
    private final CountDownLatch openLatch = new CountDownLatch(1);

    protected final StreamChannelRepository streamChannelRepository;

    private List<StreamChannelStateChangeEventHandler> stateChangeEventHandlers = new CopyOnWriteArrayList<StreamChannelStateChangeEventHandler>();

    public StreamChannel(Channel channel, int streamId, StreamChannelRepository streamChannelRepository) {
        this.channel = channel;
        this.streamChannelId = streamId;
        this.streamChannelRepository = streamChannelRepository;
    }

    public void addStateChangeEventHandler(StreamChannelStateChangeEventHandler stateChangeEventHandler) {
        stateChangeEventHandlers.add(stateChangeEventHandler);
    }

    public List<StreamChannelStateChangeEventHandler> getStateChangeEventHandlers() {
        return new ArrayList<StreamChannelStateChangeEventHandler>(stateChangeEventHandlers);
    }

    boolean changeStateOpen() {
        return changeStateTo(StreamChannelStateCode.OPEN);
    }

    boolean changeStateConnected() {
        try {
            return changeStateTo(StreamChannelStateCode.CONNECTED);
        } finally {
            openLatch.countDown();
        }
    }

    boolean changeStateClose() {
        try {
            if (state.checkState(StreamChannelStateCode.CLOSED)) {
                return true;
            }
            return changeStateTo(StreamChannelStateCode.CLOSED);
        } finally {
            openLatch.countDown();
        }
    }

    public boolean awaitOpen(long timeoutMillis) {
        try {
            openLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            return state.checkState(StreamChannelStateCode.CONNECTED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return false;
    }

    public StreamChannelStateCode getCurrentState() {
        return state.getCurrentState();
    }

    public ChannelFuture sendPing(int requestId) {
        state.assertState(StreamChannelStateCode.CONNECTED);

        StreamPingPacket packet = new StreamPingPacket(streamChannelId, requestId);
        return this.channel.write(packet);
    }

    public ChannelFuture sendPong(int requestId) {
        state.assertState(StreamChannelStateCode.CONNECTED);

        StreamPongPacket packet = new StreamPongPacket(streamChannelId, requestId);
        return this.channel.write(packet);
    }

    public void close() {
        close(StreamCode.STATE_CLOSED);
    }

    public void close(StreamCode code) {
        clearStreamChannelResource();
        if (!StreamCode.isConnectionError(code)) {
            sendClose(streamChannelId, code);
        }
    }

    public void close(StreamPacket streamPacket) {
        clearStreamChannelResource();
        channel.write(streamPacket);
    }

    private void clearStreamChannelResource() {
        streamChannelRepository.unregister(this);
        changeStateClose();
    }

    private ChannelFuture sendClose(int streamChannelId, StreamCode code) {
        if (channel.isConnected()) {
            StreamClosePacket packet = new StreamClosePacket(streamChannelId, code);
            return this.channel.write(packet);
        } else {
            return null;
        }
    }

    public void disconnect() {
        disconnect(StreamCode.STATE_CLOSED);
    }

    public void disconnect(StreamCode streamCode) {
        logger.info("{} disconnected. from remote streamChannel caused:{}", this, streamCode);
        clearStreamChannelResource();
    }

    public SocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    public int getStreamId() {
        return streamChannelId;
    }

    protected boolean changeStateTo(StreamChannelStateCode nextState) {
        StreamChannelStateCode currentState = getCurrentState();

        boolean isChanged = state.to(currentState, nextState);
        if (!isChanged && (getCurrentState() != StreamChannelStateCode.ILLEGAL_STATE)) {
            changeStateTo(StreamChannelStateCode.ILLEGAL_STATE);
        }

        if (isChanged) {
            for (StreamChannelStateChangeEventHandler handler : stateChangeEventHandlers) {
                try {
                    handler.eventPerformed(this, nextState);
                } catch (Exception e) {
                    handler.exceptionCaught(this, nextState, e);
                }
            }
        }

        return isChanged;
    }

    public final Object getAttribute(String key) {
        return attribute.get(key);
    }

    public final Object setAttributeIfAbsent(String key, Object value) {
        return attribute.putIfAbsent(key, value);
    }

    public final Object removeAttribute(String key) {
        return attribute.remove(key);
    }

    abstract void handleStreamClose(StreamClosePacket packet);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());

        sb.append("[Channel:");
        sb.append(channel);
        sb.append(", StreamId:");
        sb.append(getStreamId());
        sb.append(", State:");
        sb.append(getCurrentState());
        sb.append("].");

        return sb.toString();
    }

}

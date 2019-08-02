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

import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author koo.taejin
 */
public abstract class AbstractStreamChannel implements StreamChannel {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentHashMap<String, Object> attribute = new ConcurrentHashMap<String, Object>();

    private final int streamChannelId;
    protected final StreamChannelState state  = new StreamChannelState();
    private final CountDownLatch openLatch = new CountDownLatch(1);

    protected final StreamChannelRepository streamChannelRepository;

    public AbstractStreamChannel(int streamId, StreamChannelRepository streamChannelRepository) {
        this.streamChannelId = streamId;
        this.streamChannelRepository = streamChannelRepository;
    }

    protected abstract StreamChannelStateChangeEventHandler getStateChangeEventHandler();

    @Override
    public void init() throws StreamException {
        changeStateTo(StreamChannelStateCode.OPEN, true);
        streamChannelRepository.registerIfAbsent(this);
    }

    @Override
    public boolean changeStateConnected() {
        try {
            return changeStateTo(StreamChannelStateCode.CONNECTED);
        } finally {
            openLatch.countDown();
        }
    }

    private boolean changeStateClose() {
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

    @Override
    public StreamChannelStateCode getCurrentState() {
        return state.getCurrentState();
    }

    @Override
    public void close() {
        close(StreamCode.STATE_CLOSED);
    }

    protected void clearStreamChannelResource() {
        streamChannelRepository.unregister(this);
        changeStateClose();
    }

    @Override
    public void disconnect() {
        disconnect(StreamCode.STATE_CLOSED);
    }

    @Override
    public void disconnect(StreamCode streamCode) {
        logger.info("disconnect. local => {}(streamId:{}, state:{}) message:{}", getRemoteAddress(), getStreamId(), getCurrentState(), streamCode);
        clearStreamChannelResource();
    }

    @Override
    public int getStreamId() {
        return streamChannelId;
    }

    protected boolean changeStateTo(StreamChannelStateCode nextState, boolean throwException) throws StreamException {
        StreamChannelStateCode currentState = getCurrentState();
        boolean changed = changeStateTo(currentState, nextState);
        if (!changed && throwException) {
            throw new StreamException(StreamCode.STATE_ERROR, "Failed to change state. updateWanted:<" + nextState + ">, current:<" + currentState + ">");
        }
        return changed;
    }

    protected boolean changeStateTo(StreamChannelStateCode nextState) {
        StreamChannelStateCode currentState = getCurrentState();
        return changeStateTo(currentState, nextState);
    }

    protected boolean changeStateTo(StreamChannelStateCode currentState, StreamChannelStateCode nextState) {
        boolean isChanged = state.to(currentState, nextState);
        if (!isChanged && (getCurrentState() != StreamChannelStateCode.ILLEGAL_STATE)) {
            changeStateTo(StreamChannelStateCode.ILLEGAL_STATE);
        }

        if (isChanged) {
            try {
                getStateChangeEventHandler().stateUpdated(this, nextState);
            } catch (Exception e) {
                logger.warn("Please handling exception in StreamChannelStateChangeEventHandler.stateUpdated method. message:{}", e.getMessage(), e);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());

        sb.append("[RemoteAddress:");
        sb.append(getRemoteAddress());
        sb.append(", StreamId:");
        sb.append(getStreamId());
        sb.append(", State:");
        sb.append(getCurrentState());
        sb.append("].");

        return sb.toString();
    }

}

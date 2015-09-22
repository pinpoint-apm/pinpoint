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

package com.navercorp.pinpoint.rpc.client;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;

public class PinpointClientHandshaker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ChannelFutureListener handShakeFailFutureListener = new WriteFailFutureListener(this.logger, "HandShakePacket write fail.", "HandShakePacket write success.");
    
    private static final int STATE_INIT = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_FINISHED = 2;
    // STATE_INIT -> STATE_STARTED -> STATE_COMPLETED
    // STATE_INIT -> STATE_STARTED -> STATE_ABORTED
    private final AtomicInteger state; 
    
    private final AtomicInteger handshakeCount;

    private final Timer handshakerTimer;
    private final int retryInterval;
    private final int maxHandshakeCount;
    
    private final Object lock = new Object();
    private final AtomicReference<HandshakeResponseCode> handshakeResult = new AtomicReference<HandshakeResponseCode>(null);
    
    private String simpleName;
    
    public PinpointClientHandshaker(Timer handshakerTimer, int retryInterval, int maxHandshakeCount) {
        AssertUtils.assertNotNull(handshakerTimer, "handshakerTimer may not be null.");
        AssertUtils.assertTrue(retryInterval > 0, "retryInterval must greater than zero.");
        AssertUtils.assertTrue(maxHandshakeCount > 0, "maxHandshakeCount must greater than zero.");
        
        this.state = new AtomicInteger(STATE_INIT);
        this.handshakerTimer = handshakerTimer;
        this.retryInterval = retryInterval;
        this.maxHandshakeCount = maxHandshakeCount;
        
        this.handshakeCount = new AtomicInteger(0);
    }
 
    public void handshakeStart(Channel channel, Map<String, Object> handshakeData) {
        logger.info("{} handshakeStart method started.", simpleClassNameAndHashCodeString());
        
        if (channel == null) {
            logger.info("{} handshakeStart method failed. channel may not be null.", simpleClassNameAndHashCodeString());
            return;
        }
        
        if (!channel.isConnected()) {
            logger.info("{} handshakeStart method failed. channel is not connected.", simpleClassNameAndHashCodeString());
            return;
        }

        if (!state.compareAndSet(STATE_INIT, STATE_STARTED)) {
            logger.info("{} handshakeStart method failed. currentState:{}", simpleClassNameAndHashCodeString(), state.get());
            return;
        }
        
        HandshakeJob handshakeJob = null;
        try {
            handshakeJob = createHandshakeJob(channel, handshakeData);
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(simpleClassNameAndHashCodeString() + " create handshake job failed. Error:" + e.getMessage() + " state will be aborted.", e);
            }
        }

        if (handshakeJob == null) {
            handshakeAbort();
            logger.info("{} handshakeStart method failed.", simpleClassNameAndHashCodeString());
            return;
        }

        handshake(handshakeJob);
        reservationJob(handshakeJob);
        logger.info("{} handshakeStart method completed. channel:{}, data:{}", simpleClassNameAndHashCodeString(), channel, handshakeData);
    }

    private HandshakeJob createHandshakeJob(Channel channel, Map<String, Object> handshakeData) throws ProtocolException {
        byte[] payload = ControlMessageEncodingUtils.encode(handshakeData);
        ControlHandshakePacket handshakePacket = new ControlHandshakePacket(payload);

        HandshakeJob handshakeJob = new HandshakeJob(channel, handshakePacket);
        return handshakeJob;
    }
    
    private void handshake(HandshakeJob handshakeJob) {
        handshakeCount.incrementAndGet();
        
        Channel channel = handshakeJob.getChannel();
        ControlHandshakePacket packet = handshakeJob.getHandshakePacket();
        
        final ChannelFuture future = channel.write(packet);
        
        logger.debug("{} handshakePacket sent. channel:{}, packet:{}.", simpleClassNameAndHashCodeString(), channel, packet);
        
        future.addListener(handShakeFailFutureListener);
    }

    private void reservationJob(HandshakeJob handshake) {
        if (handshakeCount.get() >= maxHandshakeCount) {
            handshakeAbort();
            return;
        }

        this.handshakerTimer.newTimeout(handshake, retryInterval, TimeUnit.MILLISECONDS);
    }
    
    public boolean handshakeComplete(ControlHandshakeResponsePacket message) {
        logger.info("{} handshakeComplete method started. params:{}", simpleClassNameAndHashCodeString(), message);

        synchronized (lock) {
            if (!this.state.compareAndSet(STATE_STARTED, STATE_FINISHED)) {
                // state can be 0 or 2.
                logger.info("{} handshakeComplete method failed. beforeState:{}", simpleClassNameAndHashCodeString(), state.get());
                this.state.set(STATE_FINISHED);
                return false;
            }
            
            HandshakeResponseCode code = getHandshakeResponseCode(message);
            handshakeResult.compareAndSet(null, code);
            logger.info("{} handshakeComplete method completed. handshakeResult:{} / {}", simpleClassNameAndHashCodeString(), code, handshakeResult.get());
            return true;
        }
    }

    private HandshakeResponseCode getHandshakeResponseCode(ControlHandshakeResponsePacket message) {
        byte[] payload = message.getPayload();
        if (payload == null) {
            return HandshakeResponseCode.PROTOCOL_ERROR;
        }
        
        try {
            Map result = (Map) ControlMessageEncodingUtils.decode(payload);

            int code = MapUtils.getInteger(result, ControlHandshakeResponsePacket.CODE, -1);
            int subCode = MapUtils.getInteger(result, ControlHandshakeResponsePacket.SUB_CODE, -1);
            
            return HandshakeResponseCode.getValue(code, subCode);
        } catch (ProtocolException e) {
            logger.warn(e.getMessage(), e);
        }
        
        return HandshakeResponseCode.UNKNOWN_CODE;
    }
    
    public HandshakeResponseCode getHandshakeResult() {
        return handshakeResult.get();
    }

    public void handshakeAbort() {
        logger.info("{} handshakeAbort method started.", simpleClassNameAndHashCodeString());

        if (!state.compareAndSet(STATE_STARTED, STATE_FINISHED)) {
            // state can be 0 or 2.
            logger.info("{} handshakeStart method failed. beforeState:{}", simpleClassNameAndHashCodeString(), state.get());
            this.state.set(STATE_FINISHED);
            return;
        }
        logger.info("{} handshakeAbort method completed.", simpleClassNameAndHashCodeString());
    }
    
    public boolean isRun() {
        int currentState = currentState();
        return isRun(currentState);
    }
    
    private boolean isRun(int currentState) {
        if (currentState == STATE_STARTED) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFinished() {
        int currentState = currentState();
        return isFinished(currentState);
    }

    private boolean isFinished(int currentState) {
        return this.state.get() == STATE_FINISHED;
    }

    private int currentState() {
        synchronized (lock) {
            return this.state.get();
        }
    }
    
    private String simpleClassNameAndHashCodeString() {
        if (simpleName == null) {
            simpleName = ClassUtils.simpleClassNameAndHashCodeString(this);
        }
        
        return simpleName;
    }
    
    private class HandshakeJob implements TimerTask {

        private final Channel channel;
        private final ControlHandshakePacket handshakePacket;

        public HandshakeJob(Channel channel, ControlHandshakePacket handshakePacket) {
            this.channel = channel;
            this.handshakePacket = handshakePacket;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            logger.info("Do handshake ({}/{}). channel:{}.", handshakeCount.get(), maxHandshakeCount, channel);
            if (timeout.isCancelled()) {
                reservationJob(this);
                return;
            }

            int currentState = currentState();
            
            if (isRun(currentState)) {
                handshake(this);
                reservationJob(this);
            } else if (isFinished(currentState)) {
                logger.warn("Handshake already completed.");
            } else {
                logger.warn("Handshake invalid state. {}", state.get());
            }
        }

        public Channel getChannel() {
            return channel;
        }

        public ControlHandshakePacket getHandshakePacket() {
            return handshakePacket;
        }
    }

}

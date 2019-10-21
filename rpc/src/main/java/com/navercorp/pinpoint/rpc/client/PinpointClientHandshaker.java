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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.cluster.Role;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;
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
    private final long retryInterval;
    private final int maxHandshakeCount;
    
    private final Object lock = new Object();
    private final AtomicReference<HandshakeResponseCode> handshakeResult = new AtomicReference<HandshakeResponseCode>(null);
    private final AtomicReference<ClusterOption> clusterOption = new AtomicReference<ClusterOption>(null);

    private final String id = ClassUtils.simpleClassNameAndHashCodeString(this);

    private final Map<String, Object> handshakeData;


    public PinpointClientHandshaker(Map<String, Object> handshakeData, Timer handshakerTimer, long retryInterval, int maxHandshakeCount) {
        Assert.isTrue(retryInterval > 0, "retryInterval must greater than zero.");
        Assert.isTrue(maxHandshakeCount > 0, "maxHandshakeCount must greater than zero.");
        
        this.state = new AtomicInteger(STATE_INIT);
        this.handshakerTimer = Assert.requireNonNull(handshakerTimer, "handshakerTimer");
        this.handshakeData = Assert.requireNonNull(handshakeData, "handshakeData");

        this.retryInterval = retryInterval;
        this.maxHandshakeCount = maxHandshakeCount;

        this.handshakeCount = new AtomicInteger(0);
    }

    public void handshakeStart(Channel channel) {
        logger.info("{} handshakeStart() started. channel:{}", id, channel);
        
        if (channel == null) {
            logger.warn("{} handshakeStart() failed. caused:channel must not be null.", id);
            return;
        }
        
        if (!channel.isConnected()) {
            logger.warn("{} handshakeStart() failed. caused:channel is not connected.", id);
            return;
        }

        if (!state.compareAndSet(STATE_INIT, STATE_STARTED)) {
            logger.warn("{} handshakeStart() failed. caused:unexpected state.", id);
            return;
        }
        
        HandshakeJob handshakeJob = null;
        try {
            handshakeJob = createHandshakeJob(channel);
        } catch (Exception e) {
            logger.warn("{} create HandshakeJob failed. caused:{}", id, e.getMessage(), e);
        }

        if (handshakeJob == null) {
            logger.warn("{} handshakeStart() failed. caused:handshakeJob must not be null.", id);
            handshakeAbort();
            return;
        }

        handshake(handshakeJob);
        reserveHandshake(handshakeJob);
        logger.info("{} handshakeStart() completed. channel:{}, data:{}", id, channel, handshakeData);
    }

    private HandshakeJob createHandshakeJob(Channel channel) throws ProtocolException {
        byte[] payload = ControlMessageEncodingUtils.encode(handshakeData);

        ControlHandshakePacket handshakePacket = new ControlHandshakePacket(0, payload);

        HandshakeJob handshakeJob = new HandshakeJob(channel, handshakePacket);
        return handshakeJob;
    }
    
    private void handshake(HandshakeJob handshakeJob) {
        handshakeCount.incrementAndGet();
        
        Channel channel = handshakeJob.getChannel();
        ControlHandshakePacket packet = handshakeJob.getHandshakePacket();
        
        logger.info("{} do handshake({}/{}). channel:{}.", id, handshakeCount.get(), maxHandshakeCount, channel);
        final ChannelFuture future = channel.write(packet);

        future.addListener(handShakeFailFutureListener);
    }

    private void reserveHandshake(HandshakeJob handshake) {
        if (handshakeCount.get() >= maxHandshakeCount) {
            logger.warn("{} reserveHandshake() failed. caused:Retry count is over({}/{}).", id, handshakeCount.get(), maxHandshakeCount);
            handshakeAbort();
            return;
        }

        logger.debug("{} reserveHandshake() started.", id);
        this.handshakerTimer.newTimeout(handshake, retryInterval, TimeUnit.MILLISECONDS);
    }
    
    public boolean handshakeComplete(ControlHandshakeResponsePacket responsePacket) {
        logger.info("{} handshakeComplete() started. responsePacket:{}", id, responsePacket);

        synchronized (lock) {
            if (!this.state.compareAndSet(STATE_STARTED, STATE_FINISHED)) {
                // state can be 0 or 2.
                logger.info("{} handshakeComplete() failed. caused:unexpected state.", id);
                this.state.set(STATE_FINISHED);
                return false;
            }

            Map handshakeResponse = decode(responsePacket);

            HandshakeResponseCode code = getResponseCode(handshakeResponse);
            handshakeResult.compareAndSet(null, code);

            ClusterOption clusterOption = getClusterOption(handshakeResponse);
            this.clusterOption.compareAndSet(null, clusterOption);

            logger.info("{} handshakeComplete() completed. handshake-response:{}.", id, handshakeResponse);
            return true;
        }
    }

    private Map decode(ControlHandshakeResponsePacket message) {
        byte[] payload = message.getPayload();
        if (payload == null) {
            return Collections.emptyMap();
        }

        try {
            Map result = (Map) ControlMessageEncodingUtils.decode(payload);
            return result;
        } catch (ProtocolException e) {

        }

        return Collections.emptyMap();
    }

    private HandshakeResponseCode getResponseCode(Map handshakeResponse) {
        if (handshakeResponse == Collections.emptyMap()) {
            return HandshakeResponseCode.PROTOCOL_ERROR;
        }

        int code = MapUtils.getInteger(handshakeResponse, ControlHandshakeResponsePacket.CODE, -1);
        int subCode = MapUtils.getInteger(handshakeResponse, ControlHandshakeResponsePacket.SUB_CODE, -1);

        return HandshakeResponseCode.getValue(code, subCode);
    }

    private ClusterOption getClusterOption(Map handshakeResponse) {
        if (handshakeResponse == Collections.emptyMap()) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        }

        Map cluster = (Map) handshakeResponse.get(ControlHandshakeResponsePacket.CLUSTER);
        if (cluster == null) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        }

        String id = MapUtils.getString(cluster, "id", "");
        List<Role> roles = getRoles((List) cluster.get("roles"));

        if (StringUtils.isEmpty(id)) {
            return ClusterOption.DISABLE_CLUSTER_OPTION;
        } else {
            return new ClusterOption(true, id, roles);
        }
    }

    private List<Role> getRoles(List roleNames) {
        List<Role> roles = new ArrayList<Role>();
        for (Object roleName : roleNames) {
            if (roleName instanceof String && StringUtils.hasLength((String) roleName)) {
                roles.add(Role.getValue((String) roleName));
            }
        }
        return roles;
    }

    public HandshakeResponseCode getHandshakeResult() {
        return handshakeResult.get();
    }

    public ClusterOption getClusterOption() {
        return clusterOption.get();
    }

    public void handshakeAbort() {
        logger.info("{} handshakeAbort() started.", id);

        if (!state.compareAndSet(STATE_STARTED, STATE_FINISHED)) {
            // state can be 0 or 2.
            logger.info("{} unexpected state", id);
            this.state.set(STATE_FINISHED);
            return;
        }
        logger.info("{} handshakeAbort() completed.", id);
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
        return currentState == STATE_FINISHED;
    }

    private int currentState() {
        synchronized (lock) {
            return this.state.get();
        }
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
            logger.debug("{} HandshakeJob started.", id);

            if (timeout.isCancelled()) {
                reserveHandshake(this);
                return;
            }

            int currentState = currentState();
            
            if (isRun(currentState)) {
                handshake(this);
                reserveHandshake(this);
            } else if (isFinished(currentState)) {
                logger.info("{} HandshakeJob completed.", id);
            } else {
                logger.warn("{} HandshakeJob will be stop. caused:unexpected state.", id);
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

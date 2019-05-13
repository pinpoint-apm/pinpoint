/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import org.jboss.netty.util.Timer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HandshakerFactory {

    public static final String SOCKET_ID = "socketId";


    private static final int DEFAULT_ENABLE_WORKER_PACKET_RETRY_COUNT = Integer.MAX_VALUE;

    private int maxHandshakeCount = DEFAULT_ENABLE_WORKER_PACKET_RETRY_COUNT;


    private final AtomicInteger socketId;
    private final Map<String, Object> properties;

    private final ClusterOption clusterOption;
    private final ClientOption clientOption;

    public HandshakerFactory(AtomicInteger socketId, Map<String, Object> properties, ClientOption clientOption, ClusterOption clusterOption) {
        this.socketId = Assert.requireNonNull(socketId, "socketId must not be null");

        this.clusterOption = Assert.requireNonNull(clusterOption, "clusterOption must not be null");
        this.clientOption = Assert.requireNonNull(clientOption, "clientOption must not be null");
        this.properties = Assert.requireNonNull(properties, "properties must not be null");
    }

    public PinpointClientHandshaker newHandShaker(Timer channelTimer) {
        Map<String, Object> handshakeData = createHandShakeData();
        return new PinpointClientHandshaker(handshakeData, channelTimer, clientOption.getEnableWorkerPacketDelay(), maxHandshakeCount);
    }


    private Map<String, Object> createHandShakeData() {

        Map<String, Object> handshakeData = new HashMap<String, Object>(this.properties);

        final int socketId = nextSocketId();
        handshakeData.put(SOCKET_ID, socketId);

        if (clusterOption.isEnable()) {
            handshakeData.put(ControlHandshakeResponsePacket.CLUSTER, clusterOption.toMap());
        }
        return handshakeData;
    }

    private int nextSocketId() {
        return socketId.getAndIncrement();
    }
}

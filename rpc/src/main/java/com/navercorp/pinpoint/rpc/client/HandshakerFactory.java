/*
 * Copyright 2019 NAVER Corp.
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

/**
 * @author Woonduk Kang(emeroad)
 */
public class HandshakerFactory {

    public static final String SOCKET_ID = "socketId";


    private static final int DEFAULT_ENABLE_WORKER_PACKET_RETRY_COUNT = Integer.MAX_VALUE;

    private int maxHandshakeCount = DEFAULT_ENABLE_WORKER_PACKET_RETRY_COUNT;


    private final SocketIdFactory socketIdFactory;
    private final Map<String, Object> properties;

    private final ClusterOption clusterOption;
    private final ClientOption clientOption;

    public HandshakerFactory(SocketIdFactory socketIdFactory, Map<String, Object> properties, ClientOption clientOption, ClusterOption clusterOption) {
        this.socketIdFactory = Assert.requireNonNull(socketIdFactory, "socketId");

        this.clusterOption = Assert.requireNonNull(clusterOption, "clusterOption");
        this.clientOption = Assert.requireNonNull(clientOption, "clientOption");
        this.properties = Assert.requireNonNull(properties, "properties");
    }

    public PinpointClientHandshaker newHandShaker(Timer channelTimer) {
        Map<String, Object> handshakeData = createHandShakeData();
        return new PinpointClientHandshaker(handshakeData, channelTimer, clientOption.getEnableWorkerPacketDelay(), maxHandshakeCount);
    }


    private Map<String, Object> createHandShakeData() {

        Map<String, Object> handshakeData = new HashMap<String, Object>(this.properties);

        final int socketId = this.socketIdFactory.nextSocketId();
        handshakeData.put(SOCKET_ID, socketId);

        if (clusterOption.isEnable()) {
            handshakeData.put(ControlHandshakeResponsePacket.CLUSTER, clusterOption.toMap());
        }
        return handshakeData;
    }

}

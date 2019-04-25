/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.monitor.aggregate.IpPortPacketCountAggregator;
import com.navercorp.pinpoint.io.request.ServerRequest;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ServerRequestPacketCounter implements PacketCounter<ServerRequest> {

    public static final PacketCounter<ServerRequest> NO_OP = serverRequest -> {
        // do nothing
    };

    private final IpPortPacketCountAggregator ipPortPacketCountAggregator;

    public ServerRequestPacketCounter(IpPortPacketCountAggregator ipPortPacketCountAggregator) {
        this.ipPortPacketCountAggregator = Objects.requireNonNull(ipPortPacketCountAggregator, "ipPortPacketCountAggregator must not be null");
    }

    @Override
    public void increment(ServerRequest serverRequest) {
        if (serverRequest == null) {
            return;
        }
        String remoteAddress = serverRequest.getRemoteAddress();
        int port = serverRequest.getRemotePort();
        ipPortPacketCountAggregator.increment(remoteAddress, port);
    }
}

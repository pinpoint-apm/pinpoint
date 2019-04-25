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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class InetSocketAddressPacketCounter implements PacketCounter<InetSocketAddress> {

    public static final PacketCounter<InetSocketAddress> NO_OP = inetSocketAddress -> {
        // do nothing
    };

    private final IpPortPacketCountAggregator ipPortPacketCountAggregator;

    public InetSocketAddressPacketCounter(IpPortPacketCountAggregator ipPortPacketCountAggregator) {
        this.ipPortPacketCountAggregator = Objects.requireNonNull(ipPortPacketCountAggregator, "ipPortPacketCountAggregator must not be null");
    }

    @Override
    public void increment(InetSocketAddress sourceSocketAddress) {
        InetAddress sourceAddress = sourceSocketAddress.getAddress();
        if (sourceAddress == null) {
            return;
        }
        String sourceHostAddress = sourceAddress.getHostAddress();
        int sourcePort = sourceSocketAddress.getPort();
        ipPortPacketCountAggregator.increment(sourceHostAddress, sourcePort);
    }
}

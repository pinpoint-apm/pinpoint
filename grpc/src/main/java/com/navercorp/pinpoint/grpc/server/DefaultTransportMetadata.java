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

package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.util.Assert;

import java.net.InetSocketAddress;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTransportMetadata implements TransportMetadata {

    private final String debugString;
    private final InetSocketAddress remoteAddress;
    private final InetSocketAddress localAddress;
    private final Long transportId;
    private final long connectTime;
    private final Long logId;


    public DefaultTransportMetadata(String debugString, InetSocketAddress remoteAddress, InetSocketAddress localAddreess, long transportId, long connectTime, Long logId) {
        this.debugString = Assert.requireNonNull(debugString, "debugString");
        this.remoteAddress = Assert.requireNonNull(remoteAddress, "remoteAddress");
        this.localAddress = Assert.requireNonNull(localAddreess, "localAddreess");
        this.transportId = transportId;
        this.connectTime = connectTime;
        this.logId = Assert.requireNonNull(logId, "logId");
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public Long getTransportId() {
        return transportId;
    }

    @Override
    public long getConnectTime() {
        return connectTime;
    }

    @Override
    public Long getLogId() {
        return logId;
    }

    @Override
    public String toString() {
        return "DefaultTransportMetadata{" +
                "debugString='" + debugString + '\'' +
                ", remoteAddress=" + remoteAddress +
                ", localAddress=" + localAddress +
                ", transportId=" + transportId +
                ", connectTime=" + connectTime +
                ", logId=" + logId +
                '}';
    }
}

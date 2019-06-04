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

    private final InetSocketAddress remoteAddress;
    private final Long transportId;
    private final long connectTime;

    private final LastAccessTime lastAccessTime;

    public DefaultTransportMetadata(InetSocketAddress remoteAddress, long transportId, long connectTime) {
        this.remoteAddress = Assert.requireNonNull(remoteAddress, "remoteAddress must not be null");
        this.transportId = transportId;
        this.connectTime = connectTime;
        this.lastAccessTime = new LastAccessTime(connectTime);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
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
    public LastAccessTime getLastAccessTime() {
        return lastAccessTime;
    }


    @Override
    public String toString() {
        return "DefaultTransportMetadata{" +
                "remoteAddress=" + remoteAddress +
                ", transportId=" + transportId +
                ", connectTime=" + connectTime +
                ", lastAccessTime=" + lastAccessTime +
                '}';
    }
}

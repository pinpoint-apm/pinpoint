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

package com.navercorp.pinpoint.realtime.collector.receiver.grpc;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPoint;
import com.navercorp.pinpoint.realtime.collector.receiver.SupportedCommandUtils;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import io.grpc.stub.ServerCallStreamObserver;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class GrpcAgentConnection implements ClusterPoint {

    private final InetSocketAddress remoteAddress;
    private final ClusterKey clusterKey;
    private final ServerCallStreamObserver<PCmdRequest> requestObserver;
    private final List<TCommandType> supportCommandList;

    public GrpcAgentConnection(
            InetSocketAddress remoteAddress,
            ClusterKey clusterKey,
            ServerCallStreamObserver<PCmdRequest> requestObserver,
            List<Integer> supportCommandList
    ) {
        this.remoteAddress = Objects.requireNonNull(remoteAddress, "remoteAddress");
        this.clusterKey = Objects.requireNonNull(clusterKey, "clusterKey");
        this.requestObserver = Objects.requireNonNull(requestObserver, "requestObserver");
        this.supportCommandList = SupportedCommandUtils.newSupportCommandList(supportCommandList);
    }

    public void request(PCmdRequest command) {
        synchronized (this.requestObserver) {
            this.requestObserver.onNext(command);
        }
    }

    @Override
    public ClusterKey getClusterKey() {
        return this.clusterKey;
    }

    public List<TCommandType> getSupportCommandList() {
        return supportCommandList;
    }

    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public int hashCode() {
        return this.clusterKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof GrpcAgentConnection)) {
            return false;
        }

        return this.requestObserver == ((GrpcAgentConnection) obj).requestObserver;
    }

    @Override
    public String toString() {
        return String.format("GrpcAgentConnection {clusterKey: %s}", this.getClusterKey());
    }

}
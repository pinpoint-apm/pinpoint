/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import io.grpc.Attributes;
import io.grpc.Metadata;

/**
 * @author jaehong.kim
 */
public class GrpcRequestHeader {
    public static final Attributes.Key<TransportStatus> KEY_TRANSPORT_STATUS = Attributes.Key.create("transportStatus");
    public static final Attributes.Key<Integer> KEY_TRANSPORT_ID = Attributes.Key.create("transportId");
    public static final Attributes.Key<String> KEY_REMOTE_ADDRESS = Attributes.Key.create("remoteAddress");
    public static final Attributes.Key<Integer> KEY_REMOTE_PORT = Attributes.Key.create("remotePort");
    public static final Metadata.Key<String> KEY_AGENT_ID = Metadata.Key.of("AGENT_ID", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> KEY_START_TIMESTAMP = Metadata.Key.of("START_TIMESTAMP", Metadata.ASCII_STRING_MARSHALLER);

    private TransportStatus transportStatus;
    private String remoteAddress;
    private int remotePort;
    private int transportId;
    private String agentId;
    private long startTimestamp;

    public TransportStatus getTransportStatus() {
        return transportStatus;
    }

    public void setTransportStatus(TransportStatus transportStatus) {
        this.transportStatus = transportStatus;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getTransportId() {
        return transportId;
    }

    public void setTransportId(int transportId) {
        this.transportId = transportId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcRequestHeader{");
        sb.append("remoteAddress='").append(remoteAddress).append('\'');
        sb.append(", remotePort=").append(remotePort);
        sb.append(", transportId=").append(transportId);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append('}');
        return sb.toString();
    }
}

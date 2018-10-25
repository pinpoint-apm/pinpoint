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

import com.navercorp.pinpoint.common.util.StringUtils;
import io.grpc.Attributes;
import io.grpc.Metadata;

public class GrpcRequestHeaderReader {
    public GrpcRequestHeader read(final Attributes attributes, final Metadata metadata) {
        final GrpcRequestHeader requestHeader = new GrpcRequestHeader();

        // Read attributes
        final TransportStatus transportStatus = attributes.get(GrpcRequestHeader.KEY_TRANSPORT_STATUS);
        if (transportStatus == null) {
            requestHeader.setTransportStatus(TransportStatus.INTERNAL_ERROR);
            return requestHeader;
        }
        // Set transport status
        requestHeader.setTransportStatus(transportStatus);
        if (!requestHeader.getTransportStatus().isOk()) {
            return requestHeader;
        }

        final int transportId = attributes.get(GrpcRequestHeader.KEY_TRANSPORT_ID);
        final String remoteAddress = attributes.get(GrpcRequestHeader.KEY_REMOTE_ADDRESS);
        final int remotePort = attributes.get(GrpcRequestHeader.KEY_REMOTE_PORT);
        requestHeader.setTransportId(transportId);
        requestHeader.setRemoteAddress(remoteAddress);
        requestHeader.setRemotePort(remotePort);

        // Read metadata
        final String agentId = metadata.get(GrpcRequestHeader.KEY_AGENT_ID);
//        if (StringUtils.isEmpty(agentId)) {
//            throw new IllegalArgumentException("not found " + GrpcRequestHeader.KEY_AGENT_ID.name());
//        }
        final String startTimestamp = metadata.get(GrpcRequestHeader.KEY_START_TIMESTAMP);
//        requestHeader.setAgentId(agentId);
//        requestHeader.setStartTimestamp(Long.parseLong(startTimestamp));

        return requestHeader;
    }
}
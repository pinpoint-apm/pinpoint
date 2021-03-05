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

import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.Status;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TransportMetadataFactory {

    private static final AtomicLong idGenerator = new AtomicLong(0);
    private final String debugString;

    public TransportMetadataFactory(String debugString) {
        this.debugString = Objects.requireNonNull(debugString, "debugString");
    }

    public TransportMetadata build(Attributes attributes) {
        final InetSocketAddress remoteSocketAddress = (InetSocketAddress) attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (remoteSocketAddress == null) {
            // Unauthenticated
            throw Status.INTERNAL.withDescription("RemoteSocketAddress is null").asRuntimeException();
        }
        final InetSocketAddress localSocketAddress = (InetSocketAddress) attributes.get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR);
        if (localSocketAddress == null) {
            // Unauthenticated
            throw Status.INTERNAL.withDescription("LocalSocketAddress is null").asRuntimeException();
        }

        final long transportId = idGenerator.getAndIncrement();
        final long connectedTime = System.currentTimeMillis();

        final Long logId = attributes.get(MetadataServerTransportFilter.LOG_ID);
        if (logId == null) {
            throw Status.INTERNAL.withDescription("LogId not found").asRuntimeException();
        }
        return new DefaultTransportMetadata(debugString, remoteSocketAddress, localSocketAddress, transportId, connectedTime, logId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransportMetadataFactory{");
        sb.append("debugString='").append(debugString).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

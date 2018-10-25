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

import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.util.StringUtils;
import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultServerTransportFilter extends ServerTransportFilter {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AddressFilter addressFilter;

    public void setAddressFilter(AddressFilter addressFilter) {
        this.addressFilter = addressFilter;
    }

    @Override
    public Attributes transportReady(Attributes attributes) {
        final Attributes newAttributes = setup(attributes);
        if (logger.isDebugEnabled()) {
            logger.debug("Ready attributes={}", attributes);
        }
        return newAttributes;
    }

    private Attributes setup(final Attributes attributes) {
        final Attributes.Builder builder = attributes.toBuilder();
        final InetSocketAddress remoteSocketAddress = (InetSocketAddress) attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (remoteSocketAddress == null) {
            // Unauthenticated
            builder.set(GrpcRequestHeader.KEY_TRANSPORT_STATUS, TransportStatus.UNAUTHENTICATED);
            return builder.build();
        }

        if (!auth(remoteSocketAddress)) {
            // Permission denied
            builder.set(GrpcRequestHeader.KEY_TRANSPORT_STATUS, TransportStatus.PERMISSION_DENIED);
            return builder.build();
        }

        // Set remote address and port
        final String remoteAddress = SocketAddressUtils.getAddressFirst(remoteSocketAddress);
        if (StringUtils.isEmpty(remoteAddress)) {
            // Invalid argument
            builder.set(GrpcRequestHeader.KEY_TRANSPORT_STATUS, TransportStatus.INVALID_ARGUMENT);
            return builder.build();
        }
        final int remotePort = remoteSocketAddress.getPort();
        builder.set(GrpcRequestHeader.KEY_REMOTE_ADDRESS, remoteAddress);
        builder.set(GrpcRequestHeader.KEY_REMOTE_PORT, remotePort);

        // Set transport id
        final int transportId = idGenerator.getAndIncrement();
        builder.set(GrpcRequestHeader.KEY_TRANSPORT_ID, transportId);

        builder.set(GrpcRequestHeader.KEY_TRANSPORT_STATUS, TransportStatus.OK);
        return builder.build();
    }

    private boolean auth(final InetSocketAddress remoteSocketAddress) {
        final InetAddress inetAddress = remoteSocketAddress.getAddress();
        if (this.addressFilter != null && !addressFilter.accept(inetAddress)) {
            return false;
        }
        return true;
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        if (logger.isDebugEnabled()) {
            logger.debug("Terminated attributes={}", transportAttrs);
        }
    }
}
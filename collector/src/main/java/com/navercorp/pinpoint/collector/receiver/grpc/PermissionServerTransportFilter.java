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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.common.server.util.AddressFilter;
import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class PermissionServerTransportFilter extends ServerTransportFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AddressFilter addressFilter;

    public PermissionServerTransportFilter(final AddressFilter addressFilter) {
        this.addressFilter = Objects.requireNonNull(addressFilter, "addressFilter must not be null");
    }

    @Override
    public Attributes transportReady(final Attributes attributes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Ready attributes={}", attributes);
        }

        final InetSocketAddress remoteSocketAddress = (InetSocketAddress) attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (remoteSocketAddress == null) {
            // Unauthenticated
            logger.debug("Unauthenticated transport. TRANSPORT_ATTR_REMOTE_ADDR must not be null");
            throw Status.INTERNAL.withDescription("RemoteAddress is null").asRuntimeException();
        }

        final InetAddress inetAddress = remoteSocketAddress.getAddress();
        if (addressFilter.accept(inetAddress)) {
            return attributes;
        }

        // Permission denied
        logger.debug("Permission denied transport.");
        throw Status.PERMISSION_DENIED.withDescription("invalid IP").asRuntimeException();
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        if (logger.isDebugEnabled()) {
            logger.debug("Terminated attributes={}", transportAttrs);
        }
    }
}

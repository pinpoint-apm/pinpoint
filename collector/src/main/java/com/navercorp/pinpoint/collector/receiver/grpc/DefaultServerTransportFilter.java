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
import com.navercorp.pinpoint.common.util.StringUtils;
import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author jaehong.kim
 */
public class DefaultServerTransportFilter extends ServerTransportFilter {
    public static final Attributes.Key<String> KEY_REMOTE_ADDRESS = Attributes.Key.create("remoteAddress");
    public static final Attributes.Key<Integer> KEY_REMOTE_PORT = Attributes.Key.create("remotePort");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Attributes transportReady(Attributes attributes) {
        final Attributes newAttributes = setup(attributes);
        if (logger.isDebugEnabled()) {
            logger.debug("Ready attributes={}", newAttributes);
        }
        return newAttributes;
    }

    private Attributes setup(final Attributes attributes) {
        final InetSocketAddress remoteSocketAddress = (InetSocketAddress) attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (remoteSocketAddress == null) {
            logger.debug("Not found TRANSPORT_ATTR_REMOTE_ADDR. attributes={}", attributes);
            return attributes;
        }

        final Attributes.Builder builder = attributes.toBuilder();
        // Set remote address and port
        final String remoteAddress = SocketAddressUtils.getAddressFirst(remoteSocketAddress);
        if (StringUtils.isEmpty(remoteAddress)) {
            // Invalid argument
            if (logger.isDebugEnabled()) {
                logger.debug("Internal error transport. remoteAddress={}", remoteAddress);
            }
            return attributes;
        }

        final int remotePort = remoteSocketAddress.getPort();
        builder.set(KEY_REMOTE_ADDRESS, remoteAddress);
        builder.set(KEY_REMOTE_PORT, remotePort);
        return builder.build();
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        if (logger.isDebugEnabled()) {
            logger.debug("Terminated attributes={}", transportAttrs);
        }
    }
}
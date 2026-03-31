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
import io.grpc.ServerTransportFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class MetadataServerTransportFilter extends ServerTransportFilter {

    public static final Attributes.Key<TransportMetadata> TRANSPORT_METADATA_KEY = Attributes.Key.create("transportMetadata");
    public static final Attributes.Key<TransportMutableContext> TRANSPORT_MUTABLE_CONTEXT_KEY = Attributes.Key.create("transportMutableContext");

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final TransportMetadataFactory transportMetadataFactory;

    public MetadataServerTransportFilter(TransportMetadataFactory transportMetadataFactory) {
        this.transportMetadataFactory = Objects.requireNonNull(transportMetadataFactory, "transportMetadataFactory");
    }

    @Override
    public Attributes transportReady(Attributes attributes) {
        final TransportMetadata transportMetadata = transportMetadataFactory.build(attributes);
        if (logger.isDebugEnabled()) {
            logger.debug("transportReady transportMetadata={}", transportMetadata);
        }

        Attributes.Builder builder = attributes.toBuilder();
        builder.set(TRANSPORT_METADATA_KEY, transportMetadata);
        builder.set(TRANSPORT_MUTABLE_CONTEXT_KEY, new TransportMutableContext());
        return builder.build();
    }


    @Override
    public void transportTerminated(Attributes transportAttrs) {
        if (logger.isDebugEnabled()) {
            logger.debug("transportTerminated attributes={}", transportAttrs);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetadataServerTransportFilter{");
        sb.append("transportMetadataFactory=").append(transportMetadataFactory);
        sb.append('}');
        return sb.toString();
    }
}
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

package com.navercorp.pinpoint.grpc.server.lifecycle;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import io.grpc.Attributes;
import io.grpc.ServerTransportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LifecycleTransportFilter extends ServerTransportFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LifecycleRegistry lifecycleRegistry;
    private final LifecycleListener lifecycleListener;

    public LifecycleTransportFilter(LifecycleRegistry lifecycleRegistry, LifecycleListener lifecycleListener) {
        this.lifecycleRegistry = Assert.requireNonNull(lifecycleRegistry, "lifecycleRegistry must not be null");
        this.lifecycleListener = Assert.requireNonNull(lifecycleListener, "lifecycleListener must not be null");
    }

    @Override
    public Attributes transportReady(Attributes transportAttrs) {
        TransportMetadata transportMetadata = getTransportMetadata(transportAttrs);

        Lifecycle lifecycle = new Lifecycle(transportMetadata);
        lifecycleRegistry.add(transportMetadata.getTransportId(), lifecycle);
        lifecycleListener.connect(lifecycle);

        return transportAttrs;
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        final TransportMetadata transportMetadata = getTransportMetadata(transportAttrs);

        final Lifecycle remove = lifecycleRegistry.remove(transportMetadata.getTransportId());
        lifecycleListener.close(remove);
    }

    private TransportMetadata getTransportMetadata(Attributes attributes) {
        TransportMetadata transportMetadata = attributes.get(MetadataServerTransportFilter.TRANSPORT_METADATA_KEY);
        Assert.requireNonNull(transportMetadata, "transportMetadata must not be null");
        return transportMetadata;
    }
}

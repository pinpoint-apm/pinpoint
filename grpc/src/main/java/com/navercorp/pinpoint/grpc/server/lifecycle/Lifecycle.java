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
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Lifecycle {
    private final AtomicReference<AgentHeaderFactory.Header> ref = new AtomicReference<AgentHeaderFactory.Header>();
    private final TransportMetadata transportMetadata;

    public Lifecycle(TransportMetadata transportMetadata) {
        this.transportMetadata = Assert.requireNonNull(transportMetadata, "transportMetadata must not be null");
    }

    public AgentHeaderFactory.Header getRef() {
        return this.ref.get();
    }

    public boolean setInitialRef(AgentHeaderFactory.Header header) {
        Assert.requireNonNull(header, "header must not be null");
        return this.ref.compareAndSet(null, header);
    }


    public long getId() {
        return transportMetadata.getTransportId();
    }

    public TransportMetadata getTransportMetadata() {
        return transportMetadata;
    }

    @Override
    public String toString() {
        return "Lifecycle{" +
                "ref=" + ref +
                ", transportMetadata=" + transportMetadata +
                '}';
    }
}

/*
 * Copyright 2022 NAVER Corp.
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
package com.navercorp.pinpoint.collector.receiver.grpc;

import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author youngjin.kim2
 */
public class RemoteTrackingTransportFilter extends ServerTransportFilter {

    private final Set<InetSocketAddress> remotes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public RemoteTrackingTransportFilter() {}

    @Override
    public Attributes transportReady(Attributes attributes) {
        final InetSocketAddress remote = extract(attributes);
        if (Objects.nonNull(remote)) {
            remotes.add(remote);
        }

        return attributes;
    }


    @Override
    public void transportTerminated(Attributes attributes) {
        final InetSocketAddress remote = extract(attributes);
        if (Objects.nonNull(remote)) {
            remotes.remove(remote);
        }
    }

    private InetSocketAddress extract(Attributes attr) {
        final InetSocketAddress remote = (InetSocketAddress) attr.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        if (Objects.isNull(remote)) {
            return null;
        }

        return remote;
    }

    public boolean has(InetSocketAddress q) {
        return remotes.contains(q);
    }
}

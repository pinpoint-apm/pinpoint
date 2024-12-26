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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionCountServerTransportFilter extends ServerTransportFilter {

    private static final Attributes.Key<AtomicInteger> TERMINATED = Attributes.Key.create("TransportTerminated");

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final AtomicLong currentConnection = new AtomicLong();

    public ConnectionCountServerTransportFilter() {
    }


    @Override
    public Attributes transportReady(Attributes transportAttrs) {
        Attributes.Builder builder = transportAttrs.toBuilder();
        builder.set(TERMINATED, new AtomicInteger());
        Attributes attributes = builder.build();

        currentConnection.incrementAndGet();
        return attributes;
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        if (transportAttrs == null) {
            // transportTerminated() can be called before transportReady()
            return;
        }
        final AtomicInteger terminated = transportAttrs.get(TERMINATED);
        if (terminated == null) {
            return;
        }
        if (terminated.getAndIncrement() == 0) {
            currentConnection.decrementAndGet();
        } else {
            logger.info("transportTerminated() already terminated attribute:{}", transportAttrs);
        }
    }

    public long getCurrentConnection() {
        return currentConnection.get();
    }
}

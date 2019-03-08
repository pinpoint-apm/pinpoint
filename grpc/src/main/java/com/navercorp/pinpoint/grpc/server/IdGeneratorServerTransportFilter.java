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

package com.navercorp.pinpoint.grpc.server;

import io.grpc.Attributes;
import io.grpc.ServerTransportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static com.navercorp.pinpoint.grpc.AgentHeaderFactory.KEY_TRANSPORT_ID;

/**
 * @author jaehong.kim
 */
public class IdGeneratorServerTransportFilter extends ServerTransportFilter {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Attributes transportReady(Attributes attributes) {
        // Set transport id
        final int transportId = idGenerator.getAndIncrement();
        final Attributes.Builder builder = attributes.toBuilder();
        builder.set(KEY_TRANSPORT_ID, transportId);
        final Attributes newAttributes = builder.build();

        if (logger.isDebugEnabled()) {
            logger.debug("Ready attributes={}", newAttributes);
        }
        return newAttributes;
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        if (logger.isDebugEnabled()) {
            logger.debug("Terminated attributes={}", transportAttrs);
        }
    }
}
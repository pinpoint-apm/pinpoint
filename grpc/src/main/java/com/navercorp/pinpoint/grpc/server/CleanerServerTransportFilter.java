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
package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.grpc.server.TransportCleaner;
import io.grpc.Attributes;
import io.grpc.ServerTransportFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class CleanerServerTransportFilter extends ServerTransportFilter {

    private final Logger logger = LogManager.getLogger(this.getClass());
    public static final Attributes.Key<TransportCleaner> TRANSPORT_CLEANER_KEY = Attributes.Key.create("transportCleaner");

    @Override
    public Attributes transportReady(Attributes transportAttrs) {
        TransportCleaner cleaner = new TransportCleaner();

        Attributes.Builder builder = transportAttrs.toBuilder();
        builder.set(TRANSPORT_CLEANER_KEY, cleaner);
        return builder.build();
    }

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        final TransportCleaner cleaner = transportAttrs.get(TRANSPORT_CLEANER_KEY);
        if (Objects.isNull(cleaner)) {
            logger.warn("transport cleaner is not setup");
            return;
        }
        cleaner.startCleaning();
    }

}

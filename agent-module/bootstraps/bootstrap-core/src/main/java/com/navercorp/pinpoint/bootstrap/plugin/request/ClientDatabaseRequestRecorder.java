/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

import java.util.Objects;

public class ClientDatabaseRequestRecorder<T> {
    private static final String DEFAULT = "UNKNOWN";

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ClientDatabaseRequestAdaptor<T> requestAdaptor;

    public ClientDatabaseRequestRecorder(ClientDatabaseRequestAdaptor<T> requestAdaptor) {
        this.requestAdaptor = Objects.requireNonNull(requestAdaptor, "clientRequestAdaptor");
    }

    // Records the client's request information.
    public void record(final SpanEventRecorder recorder, final T clientRequest) {
        if (recorder == null || clientRequest == null) {
            return;
        }

        final String destinationId = requestAdaptor.getDestinationId(clientRequest);
        if (destinationId != null) {
            recorder.recordDestinationId(destinationId);
            if (isDebug) {
                logger.debug("Record destinationId={}", destinationId);
            }
        } else {
            // Set default value
            recorder.recordDestinationId(DEFAULT);
            if (isDebug) {
                logger.debug("Record destinationId={}", DEFAULT);
            }
        }

        final String endPoint = requestAdaptor.getEndPoint(clientRequest);
        if (endPoint != null) {
            recorder.recordEndPoint(endPoint);
            if (isDebug) {
                logger.debug("Record endPoint={}", endPoint);
            }
        } else {
            // Set default value
            recorder.recordEndPoint(DEFAULT);
            if (isDebug) {
                logger.debug("Record endPoint={}", DEFAULT);
            }
        }
    }
}
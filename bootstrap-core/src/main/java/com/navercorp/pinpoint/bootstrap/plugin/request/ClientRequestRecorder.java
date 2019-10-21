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
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public class ClientRequestRecorder<T> {
    private static final String DEFAULT_DESTINATION_ID = "Unknown";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final boolean param;
    private final ClientRequestAdaptor<T> clientRequestAdaptor;

    public ClientRequestRecorder(final boolean param, ClientRequestAdaptor<T> clientRequestAdaptor) {
        this.param = param;
        this.clientRequestAdaptor = Assert.requireNonNull(clientRequestAdaptor, "clientRequestAdaptor");
    }

    // Records the client's request information.
    public void record(final SpanEventRecorder recorder, final T clientRequest, final Throwable throwable) {
        if (recorder == null || clientRequest == null) {
            return;
        }

        final String destinationId = clientRequestAdaptor.getDestinationId(clientRequest);
        if (destinationId != null) {
            recorder.recordDestinationId(destinationId);
            if (isDebug) {
                logger.debug("Record destinationId={}", destinationId);
            }
        } else {
            // Set default value
            recorder.recordDestinationId(DEFAULT_DESTINATION_ID);
            if (isDebug) {
                logger.debug("Record destinationId={}", DEFAULT_DESTINATION_ID);
            }
        }

        final String url = clientRequestAdaptor.getUrl(clientRequest);
        if (url != null) {
            final String httpUrl = InterceptorUtils.getHttpUrl(url, this.param);
            recorder.recordAttribute(AnnotationKey.HTTP_URL, httpUrl);
            if (isDebug) {
                logger.debug("Record url={}", httpUrl);
            }
        }


    }



}
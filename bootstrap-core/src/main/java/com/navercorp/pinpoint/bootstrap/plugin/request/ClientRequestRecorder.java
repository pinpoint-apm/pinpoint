/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author jaehong.kim
 */
public class ClientRequestRecorder {
    private static final String DEFAULT_DESTINATION_ID = "Unknown";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final boolean param;
    private final HttpDumpConfig httpDumpConfig;

    public ClientRequestRecorder(final boolean param, final HttpDumpConfig httpDumpConfig) {
        this.param = param;
        this.httpDumpConfig = httpDumpConfig;
    }

    // Records the client's request information.
    public void record(final SpanEventRecorder recorder, final ClientRequestWrapper clientRequestWrapper, final Throwable throwable) {
        if (recorder == null || clientRequestWrapper == null) {
            return;
        }

        final String destinationId = clientRequestWrapper.getDestinationId();
        if (destinationId != null) {
            recorder.recordDestinationId(destinationId);
            if (isDebug) {
                logger.debug("Record destinationId={}", clientRequestWrapper.getDestinationId());
            }
        } else {
            // Set default value
            recorder.recordDestinationId(DEFAULT_DESTINATION_ID);
            if (isDebug) {
                logger.debug("Record destinationId={}", DEFAULT_DESTINATION_ID);
            }
        }

        final String url = clientRequestWrapper.getUrl();
        if (url != null) {
            final String httpUrl = InterceptorUtils.getHttpUrl(clientRequestWrapper.getUrl(), this.param);
            recorder.recordAttribute(AnnotationKey.HTTP_URL, httpUrl);
            if (isDebug) {
                logger.debug("Record url={}", httpUrl);
            }
        }

        final boolean isException = InterceptorUtils.isThrowable(throwable);
        if (this.httpDumpConfig.isDumpCookie()) {
            if (DumpType.ALWAYS == this.httpDumpConfig.getCookieDumpType()) {
                recordCookie(recorder, clientRequestWrapper);
            } else if (DumpType.EXCEPTION == this.httpDumpConfig.getCookieDumpType() && isException) {
                recordCookie(recorder, clientRequestWrapper);
            }
        }

        if (this.httpDumpConfig.isDumpEntity()) {
            if (DumpType.ALWAYS == this.httpDumpConfig.getEntityDumpType()) {
                recordEntity(recorder, clientRequestWrapper);
            } else if (DumpType.EXCEPTION == this.httpDumpConfig.getEntityDumpType() && isException) {
                recordEntity(recorder, clientRequestWrapper);
            }
        }
    }

    private void recordCookie(final SpanEventRecorder recorder, final ClientRequestWrapper clientRequestWrapper) {
        if (this.httpDumpConfig.getCookieSampler().isSampling()) {
            final String cookieValue = clientRequestWrapper.getCookieValue();
            if (cookieValue != null) {
                recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.abbreviate(cookieValue, this.httpDumpConfig.getCookieDumpSize()));
                if (isDebug) {
                    logger.debug("Record cookie={}", cookieValue);
                }
            }
        }
    }

    private void recordEntity(final SpanEventRecorder recorder, final ClientRequestWrapper clientRequestWrapper) {
        if (this.httpDumpConfig.getEntitySampler().isSampling()) {
            final String entityValue = clientRequestWrapper.getEntityValue();
            if (entityValue != null) {
                recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.abbreviate(entityValue, this.httpDumpConfig.getEntityDumpSize()));
                if (isDebug) {
                    logger.debug("Record entity={}", entityValue);
                }
            }
        }
    }
}
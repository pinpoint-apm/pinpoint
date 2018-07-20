/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.http;

import com.navercorp.pinpoint.bootstrap.config.HttpStatusCodeErrors;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author jaehong.kim
 */
public class HttpStatusCodeRecorder {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpStatusCodeErrors errors;

    public HttpStatusCodeRecorder(final HttpStatusCodeErrors errors) {
        this.errors = errors;
    }

    public void record(final SpanRecorder spanRecorder, final int statusCode) {
        if (spanRecorder == null) {
            // ignored
            return;
        }

        if (!this.errors.isHttpStatusCode(statusCode)) {
            if (isDebug) {
                logger.debug("Out of range HTTP status code. statusCode={}", statusCode);
            }
            return;
        }

        spanRecorder.recordStatusCode(statusCode);
        if (isDebug) {
            logger.debug("Record HTTP status code annotation. statusCode={}", statusCode);
        }
        if (this.errors.isErrorCode(statusCode)) {
            spanRecorder.recordError();
            if (isDebug) {
                logger.debug("Record error");
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HttpStatusCodeRecorder{");
        sb.append(", errors=").append(errors);
        sb.append('}');
        return sb.toString();
    }
}
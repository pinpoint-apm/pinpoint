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

package com.navercorp.pinpoint.bootstrap.plugin.request.util;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultCookieRecorder<T> implements CookieRecorder<T> {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpDumpConfig httpDumpConfig;
    private final CookieExtractor<T> cookieExtractor;

    public DefaultCookieRecorder(HttpDumpConfig httpDumpConfig, CookieExtractor<T> cookieExtractor) {
        this.httpDumpConfig = Assert.requireNonNull(httpDumpConfig, "httpDumpConfig");
        this.cookieExtractor = Assert.requireNonNull(cookieExtractor, "cookieExtractor");
    }

    @Override
    public void record(SpanEventRecorder recorder, T cookie, Throwable throwable) {

        if (DumpType.ALWAYS == this.httpDumpConfig.getCookieDumpType()) {
            recordCookie(recorder, cookie);
        } else if (DumpType.EXCEPTION == this.httpDumpConfig.getCookieDumpType() && InterceptorUtils.isThrowable(throwable)) {
            recordCookie(recorder, cookie);
        }
    }

    private void recordCookie(final SpanEventRecorder recorder, final T cookie) {
        if (this.httpDumpConfig.getCookieSampler().isSampling()) {
            final String cookieValue = cookieExtractor.getCookie(cookie);
            if (cookieValue != null) {
                final int cookieDumpSize = this.httpDumpConfig.getCookieDumpSize();
                final String cookieString = StringUtils.abbreviate(cookieValue, cookieDumpSize);
                recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, cookieString);
                if (isDebug) {
                    logger.debug("Record cookie={}", cookieValue);
                }
            }
        }
    }
}

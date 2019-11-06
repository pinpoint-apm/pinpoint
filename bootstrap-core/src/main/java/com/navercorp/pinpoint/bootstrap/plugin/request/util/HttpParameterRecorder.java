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

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpParameterRecorder<T> implements ParameterRecorder<T> {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final ParameterExtractor<T> parameterExtractor;

    public HttpParameterRecorder(ParameterExtractor<T> parameterExtractor) {
        this.parameterExtractor = Assert.requireNonNull(parameterExtractor, "parameterExtractor");
    }

    @Override
    public void record(SpanEventRecorder spanRecorder, T request, Throwable throwable) {

        final String parameters = parameterExtractor.extractParameter(request);
        if (StringUtils.hasLength(parameters)) {
            spanRecorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
            if (logger.isDebugEnabled()) {
                logger.debug("Record httpParam={}", parameters);
            }
        }
    }
}

/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

import java.util.List;

public class OpenTelemetryAnnotationValueUtils {
    public static final long DEFAULT_SPAN_ID = -1;
    public static final long DEFAULT_PARENT_SPAN_ID = -1;
    public static final long DEFAULT_START_TIME = 0;

    public static long getSpanId(List<AnnotationBo> annotationBoList) {
        return getValue(annotationBoList, AnnotationKey.OPENTELEMETRY_SPAN_ID.getCode(), DEFAULT_SPAN_ID);
    }

    public static long getParentSpanId(List<AnnotationBo> annotationBoList) {
        return getValue(annotationBoList, AnnotationKey.OPENTELEMETRY_PARENT_SPAN_ID.getCode(), DEFAULT_PARENT_SPAN_ID);
    }

    public static long getValue(List<AnnotationBo> annotationBoList, int key, long defaultValue) {
        if (annotationBoList != null && !annotationBoList.isEmpty()) {
            for (AnnotationBo annotationBo : annotationBoList) {
                if (annotationBo.getKey() == key) {
                    Object value = annotationBo.getValue();
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    }
                    return defaultValue;
                }
            }
        }

        return defaultValue;
    }

    public static Values getValues(List<AnnotationBo> annotationBoList) {
        long startTime = DEFAULT_START_TIME;
        long spanId = DEFAULT_SPAN_ID;
        long parentSpanId = DEFAULT_PARENT_SPAN_ID;

        if (annotationBoList != null && !annotationBoList.isEmpty()) {
            for (AnnotationBo annotationBo : annotationBoList) {
                Object value = annotationBo.getValue();
                if (!(value instanceof Number)) {
                    continue;
                }
                long longValue = ((Number) value).longValue();
                if (annotationBo.getKey() == AnnotationKey.OPENTELEMETRY_SPAN_ID.getCode()) {
                    spanId = longValue;
                } else if (annotationBo.getKey() == AnnotationKey.OPENTELEMETRY_PARENT_SPAN_ID.getCode()) {
                    parentSpanId = longValue;
                } else if (annotationBo.getKey() == AnnotationKey.OPENTELEMETRY_START_TIME.getCode()) {
                    startTime = longValue;
                }
            }
        }

        return new Values(startTime, spanId, parentSpanId);
    }


    public record Values(long startTime, long spanId, long parentSpanId) {
    }
}

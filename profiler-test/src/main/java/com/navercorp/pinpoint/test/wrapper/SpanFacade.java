/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.wrapper;

import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.test.util.AnnotationUtils;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SpanFacade implements ActualTrace {
    private final Span span;

    SpanFacade(Span span) {
        this.span = span;
    }

    @Override
    public Short getServiceType() {
        final short serviceType = span.getServiceType();
        if (serviceType == 0) {
            return null;
        }
        return serviceType;
    }

    @Override
    public Integer getApiId() {
        final int apiId = span.getApiId();
        if (apiId == 0) {
            return null;
        }
        return apiId;
    }

    @Override
    public Integer getAsyncId() {
        return null;
    }

    @Override
    public Integer getNextAsyncId() {
        return null;
    }

    @Override
    public IntStringValue getExceptionInfo() {
        return span.getExceptionInfo();
    }

    @Override
    public String getRpc() {
        return span.getTraceRoot().getShared().getRpcName();
    }

    @Override
    public String getEndPoint() {
        return span.getTraceRoot().getShared().getEndPoint();
    }

    @Override
    public String getRemoteAddr() {
        return span.getRemoteAddr();
    }

    @Override
    public String getDestinationId() {
        return null;
    }

    @Override
    public List<Annotation<?>> getAnnotations() {
        return span.getAnnotations();
    }

    @Override
    public Class<?> getType() {
        return Span.class;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Span(serviceType: ");
        builder.append(getServiceType());
        builder.append(", apiId: ");
        builder.append(getApiId());
        builder.append(", exception: ");
        builder.append(getExceptionInfo());
        builder.append(", rpc: ");
        builder.append(getRpc());
        builder.append(", endPoint: ");
        builder.append(getEndPoint());
        builder.append(", remoteAddr: ");
        builder.append(getRemoteAddr());
        builder.append(", annotations: [");
        AnnotationUtils.appendAnnotations(builder, getAnnotations());
        builder.append("])");

        return builder.toString();
    }


}

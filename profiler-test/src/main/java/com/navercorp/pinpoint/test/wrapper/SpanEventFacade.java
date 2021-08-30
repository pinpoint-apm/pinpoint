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
import com.navercorp.pinpoint.profiler.context.AsyncSpanChunk;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.test.util.AnnotationUtils;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SpanEventFacade implements ActualTrace {
    private final SpanChunk spanChunk;
    private final SpanEvent spanEvent;

    SpanEventFacade(SpanChunk spanChunk) {
        this.spanChunk = spanChunk;
        List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (spanEventList.size() != 1) {
            throw new IllegalArgumentException("spanChunk list size == 0");
        }
        this.spanEvent = spanEventList.get(0);
    }

    @Override
    public Short getServiceType() {
        return spanEvent.getServiceType();
    }

    @Override
    public Integer getApiId() {
        return spanEvent.getApiId();
    }

    @Override
    public Integer getAsyncId() {
        if (!(spanChunk instanceof AsyncSpanChunk)) {
            return null;
        }
        AsyncSpanChunk asyncSpanChunk = (AsyncSpanChunk) this.spanChunk;
        LocalAsyncId localAsyncId = asyncSpanChunk.getLocalAsyncId();
        return localAsyncId.getAsyncId();
    }

    @Override
    public Integer getNextAsyncId() {
        return spanEvent.getAsyncIdObject() != null ? spanEvent.getAsyncIdObject().getAsyncId() : null;
    }

    @Override
    public IntStringValue getExceptionInfo() {
        return spanEvent.getExceptionInfo();
    }

    @Override
    public String getRpc() {
        return null;
    }

    @Override
    public String getEndPoint() {
        return spanEvent.getEndPoint();
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getDestinationId() {
        return spanEvent.getDestinationId();
    }

    @Override
    public List<Annotation<?>> getAnnotations() {
        return spanEvent.getAnnotations();
    }

    @Override
    public Class<?> getType() {
        return SpanEvent.class;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SpanEvent(serviceType: ");
        builder.append(getServiceType());
        builder.append(", apiId: ");
        builder.append(getApiId());
        builder.append(", exception: ");
        builder.append(getExceptionInfo());
        builder.append(", rpc: ");
        builder.append(getRpc());
        builder.append(", endPoint: ");
        builder.append(getEndPoint());
        builder.append(", destinationId: ");
        builder.append(getDestinationId());
        builder.append(", annotations: [");
        AnnotationUtils.appendAnnotations(builder, getAnnotations());
        builder.append("], localAsyncId: ");
        builder.append(getAsyncId());
        builder.append(", nextAsyncId: ");
        builder.append(getNextAsyncId());
        builder.append(')');

        return builder.toString();
    }

}
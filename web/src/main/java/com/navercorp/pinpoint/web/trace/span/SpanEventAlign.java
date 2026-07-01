/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.span;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.web.util.OpenTelemetryAnnotationValueUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanEventAlign implements Align {
    private final SpanBo spanBo;
    private final SpanEventBo spanEventBo;
    private final boolean openTelemetry;

    private int id;
    private long gap;
    private long gapNanos;
    private int depth;
    private long executionMillis;
    private long executionNanos;
    private long openTelemetrySpanId = -1;
    private long openTelemetryParentSpanId = -1;

    public SpanEventAlign(SpanBo spanBo, SpanEventBo spanEventBo) {
        this(spanBo, spanEventBo, false);
    }

    public SpanEventAlign(SpanBo spanBo, SpanEventBo spanEventBo, boolean openTelemetry) {
        this.spanBo = Objects.requireNonNull(spanBo, "spanBo");
        this.spanEventBo = Objects.requireNonNull(spanEventBo, "spanEventBo");
        this.openTelemetry = openTelemetry;

        if (openTelemetry) {
            openTelemetrySpanId = OpenTelemetryAnnotationValueUtils.getSpanId(spanEventBo.getAnnotationBoList());
            openTelemetryParentSpanId = OpenTelemetryAnnotationValueUtils.getParentSpanId(spanEventBo.getAnnotationBoList());
        }
    }

    @Override
    public boolean isMeta() {
        return false;
    }

    @Override
    public boolean isSpan() {
        return false;
    }

    @Override
    public SpanBo getSpanBo() {
        return spanBo;
    }

    @Override
    public SpanEventBo getSpanEventBo() {
        return spanEventBo;
    }

    @Override
    public boolean hasChild() {
        return false;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public long getGapMillis() {
        return gap;
    }

    @Override
    public void setGapMillis(long gapMillis) {
        this.gap = gapMillis;
        this.gapNanos = TimeUnit.MILLISECONDS.toNanos(gapMillis);
    }

    @Override
    public long getGapNanos() {
        return gapNanos;
    }

    @Override
    public void setGapNanos(long gapNanos) {
        this.gapNanos = gapNanos;
        this.gap = TimeUnit.NANOSECONDS.toMillis(gapNanos);
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isAsyncFirst() {
        return false;
    }

    @Override
    public long getExecutionMillis() {
        return executionMillis;
    }

    @Override
    public void setExecutionMillis(long executionMillis) {
        this.executionMillis = executionMillis;
        this.executionNanos = TimeUnit.MILLISECONDS.toNanos(executionMillis);
    }

    @Override
    public long getExecutionNanos() {
        return executionNanos;
    }

    @Override
    public void setExecutionNanos(long executionNanos) {
        this.executionNanos = executionNanos;
        this.executionMillis = TimeUnit.NANOSECONDS.toMillis(executionNanos);
    }

    @Override
    public long getCollectorAcceptTime() {
        return spanBo.getCollectorAcceptTime();
    }

    @Override
    public byte getLoggingTransactionInfo() {
        return spanBo.getLoggingTransactionInfo();
    }

    @Override
    public long getStartTimeNanos() {
        if (spanEventBo.hasStartTime()) {
            return spanEventBo.getStartTimeNanos();
        }
        return spanBo.getStartTimeNanos() + TimeUnit.MILLISECONDS.toNanos(spanEventBo.getStartElapsed());
    }

    @Override
    public long getEndTimeNanos() {
        if (spanEventBo.hasEndTime()) {
            return spanEventBo.getEndTimeNanos();
        }

        return getStartTimeNanos() + TimeUnit.MILLISECONDS.toNanos(spanEventBo.getEndElapsed());
    }

    @Override
    public long getElapsedMillis() {
        return TimeUnit.NANOSECONDS.toMillis(getElapsedNanos());
    }

    @Override
    public long getElapsedNanos() {
        return getEndTimeNanos() - getStartTimeNanos();
    }


    @Override
    public String getAgentId() {
        return spanBo.getAgentId();
    }

    @Override
    public String getAgentName() {
        return spanBo.getAgentName();
    }

    @Override
    public String getApplicationName() {
        return spanBo.getApplicationName();
    }

    @Override
    public String getServiceName() {
        return spanBo.getServiceName();
    }


    @Override
    public int getApplicationServiceType() {
        return spanBo.getApplicationServiceType();
    }

    @Override
    public long getAgentStartTime() {
        return spanBo.getAgentStartTime();
    }

    @Override
    public int getServiceType() {
        return spanEventBo.getServiceType();
    }

    @Override
    public String getTransactionId() {
        return spanBo.getTransactionId().toString();
    }

    @Override
    public long getSpanId() {
        return spanBo.getSpanId();
    }

    @Override
    public boolean hasException() {
        return spanEventBo.hasException();
    }

    @Override
    public ExceptionInfo getExceptionInfo() {
        return spanEventBo.getExceptionInfo();
    }

    @Override
    public String getExceptionClass() {
        return spanEventBo.getExceptionClass();
    }

    @Override
    public void setExceptionClass(String exceptionClass) {
        spanEventBo.setExceptionClass(exceptionClass);
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRpc() {
        return null;
    }

    @Override
    public int getApiId() {
        return spanEventBo.getApiId();
    }

    @Override
    public List<AnnotationBo> getAnnotationBoList() {
        return spanEventBo.getAnnotationBoList();
    }

    @Override
    public void setAnnotationBoList(List<AnnotationBo> annotationBoList) {
        spanEventBo.setAnnotationBoList(annotationBoList);
    }

    @Override
    public List<AttributeBo> getAttributeBoList() {
        return spanEventBo.getAttributeBoList();
    }

    @Override
    public String getDestinationId() {
        return spanEventBo.getDestinationId();
    }

    @Override
    public int getAsyncId() {
        return -1;
    }

    @Override
    public boolean isOpenTelemetry() {
        return openTelemetry;
    }

    @Override
    public long getOpenTelemetrySpanId() {
        return openTelemetrySpanId;
    }

    @Override
    public long getOpenTelemetryParentSpanId() {
        return openTelemetryParentSpanId;
    }

    @Override
    public long getOpenTelemetryStartTime() {
        if (openTelemetry) {
            return getStartTimeNanos();
        }
        return -1;
    }

    @Override
    public String toString() {
        return "SpanEventAlign{" +
                "spanBo=" + spanBo.getSpanId() +
                ", spanEventBo=" + spanEventBo +
                ", openTelemetry=" + openTelemetry +
                ", id=" + id +
                ", gap=" + gap +
                ", gapNanos=" + gapNanos +
                ", depth=" + depth +
                ", executionMillis=" + executionMillis +
                ", executionNanos=" + executionNanos +
                ", openTelemetrySpanId=" + openTelemetrySpanId +
                ", openTelemetryParentSpanId=" + openTelemetryParentSpanId +
                ", openTelemetryStartTime=" + getOpenTelemetryStartTime() +
                '}';
    }

    public static class Builder {
        private final SpanBo spanBo;
        private final SpanEventBo spanEventBo;

        private int id;
        private long gap;
        private int depth;
        private long executionMillis;

        public Builder(SpanBo spanBo, SpanEventBo spanEventBo) {
            this.spanBo = spanBo;
            this.spanEventBo = spanEventBo;
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setGapMillis(long gap) {
            this.gap = gap;
            return this;
        }

        public Builder setDepth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder setExecutionMillis(long executionMillis) {
            this.executionMillis = executionMillis;
            return this;
        }

        public SpanEventAlign build() {
            SpanEventAlign result = new SpanEventAlign(this.spanBo, this.spanEventBo);
            result.setId(this.id);
            result.setGapMillis(this.gap);
            result.setDepth(this.depth);
            result.setExecutionMillis(this.executionMillis);
            return result;
        }
    }
}

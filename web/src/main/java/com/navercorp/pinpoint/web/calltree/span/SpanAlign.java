/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import java.util.List;
import java.util.Objects;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanAlign implements Align {
    private final SpanBo spanBo;
    private final boolean hasChild;
    private final boolean meta;

    private int id;
    private long gap;
    private int depth;
    private long executionMilliseconds;

    public SpanAlign(SpanBo spanBo) {
        this(spanBo, false);
    }

    public SpanAlign(SpanBo spanBo, boolean meta) {
        this.spanBo = Objects.requireNonNull(spanBo, "spanBo");
        this.hasChild = hasChild0();
        this.meta = meta;
    }

    private boolean hasChild0() {
        final List<SpanEventBo> spanEvents = this.spanBo.getSpanEventBoList();
        if (CollectionUtils.isNotEmpty(spanEvents)) {
            return true;
        }

        final List<SpanChunkBo> spanChunkBoList = spanBo.getSpanChunkBoList();
        if (CollectionUtils.isNotEmpty(spanChunkBoList)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isMeta() {
        return meta;
    }

    @Override
    public boolean isSpan() {
        return true;
    }

    @Override
    public SpanBo getSpanBo() {
        return spanBo;
    }

    @Override
    public SpanEventBo getSpanEventBo() {
        return null;
    }

    @Override
    public boolean hasChild() {
        return hasChild;
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
    public long getGap() {
        return gap;
    }

    @Override
    public void setGap(long gap) {
        this.gap = gap;
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
    public long getExecutionMilliseconds() {
        return executionMilliseconds;
    }

    @Override
    public void setExecutionMilliseconds(long executionMilliseconds) {
        this.executionMilliseconds = executionMilliseconds;
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
    public long getEndTime() {
        return spanBo.getStartTime() + spanBo.getElapsed();
    }

    @Override
    public long getStartTime() {
        return spanBo.getStartTime();
    }

    @Override
    public long getElapsed() {
        return spanBo.getElapsed();
    }

    @Override
    public String getAgentId() {
        if (isMeta()) {
            return " ";
        }

        return spanBo.getAgentId();
    }

    @Override
    public String getApplicationId() {
        if (isMeta()) {
            return " ";
        }
        return spanBo.getApplicationId();
    }

    @Override
    public long getAgentStartTime() {
        return spanBo.getAgentStartTime();
    }

    @Override
    public short getServiceType() {
        return spanBo.getServiceType();
    }

    @Override
    public String getTransactionId() {
        return TransactionIdUtils.formatString(spanBo.getTransactionId());
    }

    @Override
    public long getSpanId() {
        return spanBo.getSpanId();
    }

    @Override
    public boolean hasException() {
        return spanBo.hasException();
    }

    @Override
    public int getExceptionId() {
        return spanBo.getExceptionId();
    }

    @Override
    public String getExceptionClass() {
        return spanBo.getExceptionClass();
    }

    @Override
    public void setExceptionClass(String exceptionClass) {
        spanBo.setExceptionClass(exceptionClass);
    }

    @Override
    public String getExceptionMessage() {
        return spanBo.getExceptionMessage();
    }

    @Override
    public String getRemoteAddr() {
        return spanBo.getRemoteAddr();
    }

    @Override
    public String getRpc() {
        return spanBo.getRpc();
    }

    @Override
    public int getApiId() {
        return spanBo.getApiId();
    }

    @Override
    public List<AnnotationBo> getAnnotationBoList() {
        return spanBo.getAnnotationBoList();
    }

    @Override
    public void setAnnotationBoList(List<AnnotationBo> annotationBoList) {
        spanBo.setAnnotationBoList(annotationBoList);
    }

    @Override
    public String getDestinationId() {
        return null;
    }

    @Override
    public int getAsyncId() {
        return -1;
    }

    @Override
    public String toString() {
        return "SpanAlign{" +
                "spanBo=" + spanBo +
                ", hasChild=" + hasChild +
                ", meta=" + meta +
                ", id=" + id +
                ", gap=" + gap +
                ", depth=" + depth +
                ", executionMilliseconds=" + executionMilliseconds +
                '}';
    }
}
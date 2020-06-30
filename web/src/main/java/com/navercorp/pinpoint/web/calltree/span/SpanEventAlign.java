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

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanEventAlign implements Align {
    private final SpanBo spanBo;
    private final SpanEventBo spanEventBo;

    private int id;
    private long gap;
    private int depth;
    private long executionMilliseconds;


    public SpanEventAlign(SpanBo spanBo, SpanEventBo spanEventBo) {
        this.spanBo = Objects.requireNonNull(spanBo, "spanBo");
        this.spanEventBo = Objects.requireNonNull(spanEventBo, "spanEventBo");
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
        return getStartTime() + spanEventBo.getEndElapsed();
    }

    @Override
    public long getStartTime() {
        return spanBo.getStartTime() + spanEventBo.getStartElapsed();
    }

    @Override
    public long getElapsed() {
        return spanEventBo.getEndElapsed();
    }

    @Override
    public String getAgentId() {
        return spanBo.getAgentId();
    }

    @Override
    public String getApplicationId() {
        return spanBo.getApplicationId();
    }

    @Override
    public long getAgentStartTime() {
        return spanBo.getAgentStartTime();
    }

    @Override
    public short getServiceType() {
        return spanEventBo.getServiceType();
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
        return spanEventBo.hasException();
    }

    @Override
    public int getExceptionId() {
        return spanEventBo.getExceptionId();
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
    public String getExceptionMessage() {
        return spanEventBo.getExceptionMessage();
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
    public String getDestinationId() {
        return spanEventBo.getDestinationId();
    }

    @Override
    public int getAsyncId() {
        return -1;
    }

    @Override
    public String toString() {
        return "SpanEventAlign{" +
                "spanBo=" + spanBo +
                ", spanEventBo=" + spanEventBo +
                ", id=" + id +
                ", gap=" + gap +
                ", depth=" + depth +
                ", executionMilliseconds=" + executionMilliseconds +
                '}';
    }

}
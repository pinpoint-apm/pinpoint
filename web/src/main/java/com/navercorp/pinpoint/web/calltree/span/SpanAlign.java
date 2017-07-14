/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import java.util.List;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanAlign {
    private final SpanBo spanBo;
    private final SpanEventBo spanEventBo;
    private final boolean span;
    private final boolean hasChild;

    private int id;
    private long gap;
    private int depth;
    private long executionMilliseconds;

    public SpanAlign(SpanBo spanBo) {
        if (spanBo == null) {
            throw new NullPointerException("spanBo must not be null");
        }
        this.spanBo = spanBo;
        this.spanEventBo = null;
        this.span = true;
        List<SpanEventBo> spanEvents = this.spanBo.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEvents)) {
            this.hasChild = false;
        } else {
            this.hasChild = true;
        }
    }

    public SpanAlign(SpanBo spanBo, SpanEventBo spanEventBo) {
        if (spanBo == null) {
            throw new NullPointerException("spanBo must not be null");
        }
        if (spanEventBo == null) {
            throw new NullPointerException("spanEventBo must not be null");
        }
        this.spanBo = spanBo;
        this.spanEventBo = spanEventBo;
        this.span = false;
        this.hasChild = false;
    }

    public boolean isSpan() {
        return span;
    }

    public SpanBo getSpanBo() {
        return spanBo;
    }

    public SpanEventBo getSpanEventBo() {
        return spanEventBo;
    }

    public boolean hasChild() {
        return hasChild;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getGap() {
        return gap;
    }

    public void setGap(long gap) {
        this.gap = gap;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isAsync() {
        if (isSpan()) {
            return false;
        }

        return spanEventBo.isAsync();
    }

    public boolean isAsyncFirst() {
        if (!isAsync()) {
            return false;
        }

        return spanEventBo.getSequence() == 0;
    }

    public long getExecutionMilliseconds() {
        return executionMilliseconds;
    }

    public void setExecutionMilliseconds(long executionMilliseconds) {
        this.executionMilliseconds = executionMilliseconds;
    }

    public long getCollectorAcceptTime() {
        return spanBo.getCollectorAcceptTime();
    }

    public byte getLoggingTransactionInfo() {
        return spanBo.getLoggingTransactionInfo();
    }

    public long getLastTime() {
        if (isSpan()) {
            return spanBo.getStartTime() + spanBo.getElapsed();
        } else {
            return spanBo.getStartTime() + spanEventBo.getStartElapsed() + spanEventBo.getEndElapsed();
        }
    }

    public long getStartTime() {
        if (isSpan()) {
            return spanBo.getStartTime();
        } else {
            return spanBo.getStartTime() + spanEventBo.getStartElapsed();
        }
    }

    public long getElapsed() {
        if (isSpan()) {
            return spanBo.getElapsed();
        } else {
            return spanEventBo.getEndElapsed();
        }
    }

    public String getAgentId() {
        return spanBo.getAgentId();
    }

    public String getApplicationId() {
        return spanBo.getApplicationId();
    }

    public long getAgentStartTime() {
        return spanBo.getAgentStartTime();
    }

    public short getServiceType() {
        if (isSpan()) {
            return spanBo.getServiceType();
        }
        return spanEventBo.getServiceType();
    }

    public String getTransactionId() {
        return TransactionIdUtils.formatString(spanBo.getTransactionId());
    }
    
    public long getSpanId() {
        return spanBo.getSpanId();
    }
    
    public boolean hasException() {
        if (isSpan()) {
            return spanBo.hasException();
        }
        return spanEventBo.hasException();
    }

    public int getExceptionId() {
        if (isSpan()) {
            return spanBo.getExceptionId();
        }
        return spanEventBo.getExceptionId();
    }

    public String getExceptionClass() {
        if (isSpan()) {
            return spanBo.getExceptionClass();
        }
        return spanEventBo.getExceptionClass();
    }

    public void setExceptionClass(String exceptionClass) {
        if (isSpan()) {
            spanBo.setExceptionClass(exceptionClass);
        } else {
            spanEventBo.setExceptionClass(exceptionClass);
        }
    }
    
    public String getExceptionMessage() {
        if (isSpan()) {
            return spanBo.getExceptionMessage();
        }
        
        return spanEventBo.getExceptionMessage();
    }
    
    public String getRemoteAddr() {
        if (isSpan()) {
            return spanBo.getRemoteAddr();
        }
        
        return null;
    }

    public int getApiId() {
        if (isSpan()) {
            return spanBo.getApiId();
        } else {
            return spanEventBo.getApiId();
        }
    }

    public List<AnnotationBo> getAnnotationBoList() {
        if (isSpan()) {
            return spanBo.getAnnotationBoList();
        }
        return spanEventBo.getAnnotationBoList();
    }

    public void setAnnotationBoList(List<AnnotationBo> annotationBoList) {
        if (isSpan()) {
            spanBo.setAnnotationBoList(annotationBoList);
        } else {
            spanEventBo.setAnnotationBoList(annotationBoList);
        }
    }
    
    public String getDestinationId() {
        if (isSpan()) {
            return null;
        }
        
        return spanEventBo.getDestinationId();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{spanBo=");
        builder.append(spanBo);
        builder.append(", spanEventBo=");
        builder.append(spanEventBo);
        builder.append(", span=");
        builder.append(span);
        builder.append(", hasChild=");
        builder.append(hasChild);
        builder.append(", id=");
        builder.append(id);
        builder.append(", gap=");
        builder.append(gap);
        builder.append(", depth=");
        builder.append(depth);
        builder.append(", executionTime=");
        builder.append(executionMilliseconds);
        builder.append("}");
        return builder.toString();
    }
}

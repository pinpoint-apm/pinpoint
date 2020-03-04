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

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface Align {
    boolean isMeta();

    boolean isSpan();

    SpanBo getSpanBo();

    SpanEventBo getSpanEventBo();

    boolean hasChild();

    int getId();

    void setId(int id);

    long getGap();

    void setGap(long gap);

    int getDepth();

    void setDepth(int depth);

    boolean isAsync();

    boolean isAsyncFirst();

    long getExecutionMilliseconds();

    void setExecutionMilliseconds(long executionMilliseconds);

    long getCollectorAcceptTime();

    byte getLoggingTransactionInfo();

    long getStartTime();

    long getEndTime();

    long getElapsed();

    String getAgentId();

    String getApplicationId();

    long getAgentStartTime();

    short getServiceType();

    String getTransactionId();

    long getSpanId();

    boolean hasException();

    int getExceptionId();

    String getExceptionClass();

    void setExceptionClass(String exceptionClass);

    String getExceptionMessage();

    String getRemoteAddr();

    String getRpc();

    int getApiId();

    List<AnnotationBo> getAnnotationBoList();

    void setAnnotationBoList(List<AnnotationBo> annotationBoList);

    String getDestinationId();

    int getAsyncId();
}

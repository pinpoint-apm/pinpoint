/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.filter.responsetime;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.web.filter.Filter;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanResponseConditionFilter implements Filter<SpanBo> {

    private final ResponseTimeFilter responseTimeFilter;

    private final ExecutionTypeFilter executionErrorFilter;
    private final ErrorCheck errorCheck;

    public enum ErrorCheck {
        SPAN, SPAN_AND_SPANEVENT
    }

    public SpanResponseConditionFilter(ResponseTimeFilter responseTimeFilter, ExecutionTypeFilter executionErrorFilter, ErrorCheck errorCheck) {
        this.responseTimeFilter = Objects.requireNonNull(responseTimeFilter, "responseTimeFilter");
        this.executionErrorFilter = Objects.requireNonNull(executionErrorFilter, "executionErrorFilter");
        
        this.errorCheck = Objects.requireNonNull(errorCheck, "errorCheck");
    }

    @Override
    public boolean include(SpanBo spanBo) {
        if (responseTimeFilter.accept(spanBo.getElapsed()) == ResponseTimeFilter.REJECT) {
            return false;
        }
        return executionErrorFilter.accept(hasError(spanBo));
    }

    private boolean hasError(SpanBo spanBo) {
        switch (errorCheck) {
            case SPAN:
                return hasErrorSpan(spanBo);
            case SPAN_AND_SPANEVENT:
                return hasErrorSpan(spanBo) || hasErrorSpanEvent(spanBo);
            default:
                throw new IllegalStateException("unknown state:" + errorCheck);
        }
    }

    protected boolean hasErrorSpan(SpanBo span) {
        return span.getErrCode() > 0;
    }

    private boolean hasErrorSpanEvent(SpanBo spanBo) {
        final List<SpanEventBo> eventBoList = spanBo.getSpanEventBoList();
        if (eventBoList == null) {
            return false;
        }
        for (SpanEventBo event : eventBoList) {
            if (event.hasException()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanResponseConditionFilter{");
        sb.append("responseTimeFilter=").append(responseTimeFilter);
        sb.append(", executionErrorFilter=").append(executionErrorFilter);
        sb.append('}');
        return sb.toString();
    }
}

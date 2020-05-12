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

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.web.filter.Filter;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventResponseConditionFilter implements Filter<SpanEventBo> {

    private final ResponseTimeFilter responseTimeFilter;

    private final ExecutionTypeFilter executionErrorFilter;

    public SpanEventResponseConditionFilter(ResponseTimeFilter responseTimeFilter, ExecutionTypeFilter executionErrorFilter) {
        this.responseTimeFilter = Objects.requireNonNull(responseTimeFilter, "responseTimeFilter");
        this.executionErrorFilter = Objects.requireNonNull(executionErrorFilter, "executionErrorFilter");
    }

    @Override
    public boolean include(SpanEventBo spanEventBo) {
        if (responseTimeFilter.accept(spanEventBo.getEndElapsed()) == ResponseTimeFilter.REJECT) {
            return false;
        }
        return executionErrorFilter.accept(spanEventBo.hasException());
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanEventResponseConditionFilter{");
        sb.append("responseTimeFilter=").append(responseTimeFilter);
        sb.append(", executionErrorFilter=").append(executionErrorFilter);
        sb.append('}');
        return sb.toString();
    }
}

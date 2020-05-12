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

import com.navercorp.pinpoint.web.filter.ExecutionType;
import com.navercorp.pinpoint.web.filter.visitor.SpanVisitor;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultExecutionTypeFilter implements ExecutionTypeFilter {

    private final ExecutionType executionType;

    public static ExecutionTypeFilter newExecutionTypeFilter(Boolean includeException) {
        final ExecutionType executionType = getExecutionType(includeException);
        return new DefaultExecutionTypeFilter(executionType);

    }
    
    private static ExecutionType getExecutionType(Boolean includeException) {
        if (includeException == null) {
            return ExecutionType.ALL;
        }
        if (includeException) {
            return ExecutionType.FAIL_ONLY;
        }
        return ExecutionType.SUCCESS_ONLY;
    }
    
    public DefaultExecutionTypeFilter(ExecutionType executionType) {
        this.executionType = Objects.requireNonNull(executionType, "executionType");
    }

    @Override
    public boolean accept(boolean hasError) {
        switch (executionType) {
            case ALL: {
                return SpanVisitor.ACCEPT;
            }
            case FAIL_ONLY: {
                // is error
                if (hasError) {
                    return SpanVisitor.ACCEPT;
                }
                return SpanVisitor.REJECT;
            }
            case SUCCESS_ONLY: {
                // is success
                if (hasError == false) {
                    return SpanVisitor.ACCEPT;
                }
                return SpanVisitor.REJECT;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported ExecutionType:" + executionType);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultExecutionErrorFilter{");
        sb.append("executionType=").append(executionType);
        sb.append('}');
        return sb.toString();
    }
}

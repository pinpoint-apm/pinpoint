/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.instrument.matcher;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.List;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class DefaultMultiClassBasedMatcher implements MultiClassBasedMatcher {
    private final List<String> baseClassNames;
    private final MatcherOperand matcherOperand;

    DefaultMultiClassBasedMatcher(final List<String> baseClassNames) {
        this(baseClassNames, null);
    }

    DefaultMultiClassBasedMatcher(final List<String> baseClassNames, final MatcherOperand additional) {
        if (CollectionUtils.isEmpty(baseClassNames)) {
            throw new IllegalArgumentException("basePackageNames must not be empty");
        }
        this.baseClassNames = baseClassNames;

        this.matcherOperand = getMatcherOperand(baseClassNames, additional);
    }

    private MatcherOperand getMatcherOperand(List<String> baseClassNames, MatcherOperand additional) {
        MatcherOperand operand = joinOr(baseClassNames);
        if (operand == null) {
            throw new IllegalStateException("operand is null");
        }
        if (additional == null) {
            return operand;
        }
        // (class OR ...) AND additional
        operand = operand.and(additional);
        return operand;
    }

    private MatcherOperand joinOr(List<String> baseClassNames) {
        MatcherOperand operand = null;
        for (String baseClassName : baseClassNames) {
            if (operand == null) {
                operand = new ClassInternalNameMatcherOperand(baseClassName);
            } else {
                // class OR ...
                final MatcherOperand classMatcherOperand = new ClassInternalNameMatcherOperand(baseClassName);
                operand = operand.or(classMatcherOperand);
            }
        }
        return operand;
    }

    @Override
    public List<String> getBaseClassNames() {
        return this.baseClassNames;
    }

    @Override
    public MatcherOperand getMatcherOperand() {
        return this.matcherOperand;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultMultiClassBasedMatcher{");
        sb.append("baseClassNames=").append(baseClassNames);
        sb.append(", matcherOperand=").append(matcherOperand);
        sb.append('}');
        return sb.toString();
    }
}
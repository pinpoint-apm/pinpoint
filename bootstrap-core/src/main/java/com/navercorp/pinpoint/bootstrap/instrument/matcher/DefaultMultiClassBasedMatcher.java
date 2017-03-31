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
import com.navercorp.pinpoint.common.util.Assert;

import java.util.Collections;
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
        Assert.requireNonNull(baseClassNames, "baseClassNames must not be null");
        this.baseClassNames = Collections.unmodifiableList(baseClassNames);

        MatcherOperand operand = null;
        for (String baseClassName : this.baseClassNames) {
            final MatcherOperand classMatcherOperand = new ClassInternalNameMatcherOperand(baseClassName);
            if (operand == null) {
                operand = classMatcherOperand;
            } else {
                // class OR ...
                operand = operand.or(classMatcherOperand);
            }
        }
        if (additional != null) {
            // (class OR ...) AND additional
            operand = operand.and(additional);
        }
        this.matcherOperand = operand;
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
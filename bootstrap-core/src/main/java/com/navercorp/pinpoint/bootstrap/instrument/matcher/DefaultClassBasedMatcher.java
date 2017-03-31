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

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class DefaultClassBasedMatcher implements ClassBasedMatcher {
    private final String baseClassName;
    private final MatcherOperand matcherOperand;

    DefaultClassBasedMatcher(final String baseClassName) {
        this(baseClassName, null);
    }

    DefaultClassBasedMatcher(final String baseClassName, final MatcherOperand additional) {
        Assert.requireNonNull(baseClassName, "baseClassName must not be null");
        this.baseClassName = baseClassName;

        MatcherOperand operand = new ClassInternalNameMatcherOperand(baseClassName);
        if (additional != null) {
            // class AND additional
            operand = operand.and(additional);
        }
        this.matcherOperand = operand;
    }

    @Override
    public String getBaseClassName() {
        return this.baseClassName;
    }

    @Override
    public MatcherOperand getMatcherOperand() {
        return this.matcherOperand;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultClassBasedMatcher{");
        sb.append("baseClassName='").append(baseClassName).append('\'');
        sb.append(", matcherOperand=").append(matcherOperand);
        sb.append('}');
        return sb.toString();
    }
}

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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class DefaultPackageBasedMatcher implements PackageBasedMatcher {
    private final String basePackageName;
    private final MatcherOperand matcherOperand;

    DefaultPackageBasedMatcher(final String basePackageName) {
        this(basePackageName, null);
    }

    DefaultPackageBasedMatcher(final String basePackageName, final MatcherOperand additional) {
        Objects.requireNonNull(basePackageName, "basePackageName");
        if (!StringUtils.hasText(basePackageName)) {
            throw new IllegalArgumentException("basePackageName must not be empty");
        }
        this.basePackageName = basePackageName;

        MatcherOperand operand = new PackageInternalNameMatcherOperand(basePackageName);
        if (additional != null) {
            // package AND additional
            operand = operand.and(additional);
        }
        this.matcherOperand = operand;
    }

    @Override
    public String getBasePackageName() {
        return this.basePackageName;
    }

    @Override
    public MatcherOperand getMatcherOperand() {
        return this.matcherOperand;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultPackageBasedMatcher{");
        sb.append("basePackageName='").append(basePackageName).append('\'');
        sb.append(", matcherOperand=").append(matcherOperand);
        sb.append('}');
        return sb.toString();
    }
}
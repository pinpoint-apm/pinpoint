/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.bootstrap.instrument.matcher;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;

public class LambdaExpressionMatcher implements PackageBasedMatcher {
    static final String LAMBDA_INSTANCE_NAME_PREFIX = "$$Lambda$";
    private final String basePackageName;
    private final MatcherOperand matcherOperand;

    public LambdaExpressionMatcher(String baseClassName, String functionalInterfaceName) {
        this(baseClassName, functionalInterfaceName, null);
    }

    public LambdaExpressionMatcher(String baseClassName, String functionalInterfaceName, final MatcherOperand additional) {
        Objects.requireNonNull(baseClassName, "baseClassName");
        if (!StringUtils.hasText(baseClassName)) {
            throw new IllegalArgumentException("baseClassName must not be empty");
        }
        Objects.requireNonNull(functionalInterfaceName, "functionalInterfaceName");
        if (!StringUtils.hasText(functionalInterfaceName)) {
            throw new IllegalArgumentException("functionalInterfaceName must not be empty");
        }
        this.basePackageName = baseClassName + LAMBDA_INSTANCE_NAME_PREFIX;
        this.matcherOperand = getMatcherOperand(functionalInterfaceName, additional);
    }

    private MatcherOperand getMatcherOperand(String functionalInterfaceName, final MatcherOperand additional) {
        final MatcherOperand functionalInterfaceMatcherOperand = new InterfaceInternalNameMatcherOperand(functionalInterfaceName, false);
        MatcherOperand operand = new PackageInternalNameMatcherOperand(basePackageName);
        operand.and(functionalInterfaceMatcherOperand);
        if (additional != null) {
            // class AND additional
            operand = operand.and(additional);
        }
        return operand;
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
        return "LambdaExpressionMatcher{" +
                "basePackageName='" + basePackageName + '\'' +
                ", matcherOperand=" + matcherOperand +
                '}';
    }
}
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
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class DefaultMultiPackageBasedMatcher implements MultiPackageBasedMatcher {
    private final List<String> basePackageNames;
    private final MatcherOperand matcherOperand;

    DefaultMultiPackageBasedMatcher(final List<String> basePackageNames) {
        this(basePackageNames, null);
    }

    DefaultMultiPackageBasedMatcher(final List<String> basePackageNames, final MatcherOperand additional) {
        if (CollectionUtils.isEmpty(basePackageNames)) {
            throw new IllegalArgumentException("basePackageNames must not be empty");
        }

        final List<String> buildBasePackageName = buildBasePackageNameList(basePackageNames);
        final MatcherOperand operand = joinOr(buildBasePackageName);
        if (operand == null) {
            throw new IllegalStateException("operand is null");
        }
        this.matcherOperand = addOr(operand, additional);

        this.basePackageNames = Collections.unmodifiableList(buildBasePackageName);
    }

    private MatcherOperand addOr(MatcherOperand operand, MatcherOperand additional) {
        if (additional == null) {
            return operand;
        }
        // (package OR ...) AND additional
        operand = operand.and(additional);
        return operand;
    }

    private MatcherOperand joinOr(List<String> basePackageNames) {
        if (basePackageNames.isEmpty()) {
            throw new IllegalArgumentException("basePackageNames must not be empty ");
        }

        MatcherOperand operandGroup = null;
        for (String basePackageName : basePackageNames) {
            if (operandGroup == null) {
                operandGroup = new PackageInternalNameMatcherOperand(basePackageName);
            } else {
                // package OR ...
                final MatcherOperand packageMatcherOperand = new PackageInternalNameMatcherOperand(basePackageName);
                operandGroup = operandGroup.or(packageMatcherOperand);
            }
        }
        return operandGroup;
    }

    private List<String> buildBasePackageNameList(List<String> basePackageNames) {
        final List<String> list = new ArrayList<String>(basePackageNames.size());
        for (String basePackageName : basePackageNames) {
            // skip null and empty.
            if (StringUtils.hasText(basePackageName)) {
                list.add(basePackageName);
            }
        }
        return list;
    }

    @Override
    public List<String> getBasePackageNames() {
        return this.basePackageNames;
    }

    @Override
    public MatcherOperand getMatcherOperand() {
        return this.matcherOperand;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultMultiPackageBasedMatcher{");
        sb.append("basePackageNames=").append(basePackageNames);
        sb.append(", matcherOperand=").append(matcherOperand);
        sb.append('}');
        return sb.toString();
    }
}
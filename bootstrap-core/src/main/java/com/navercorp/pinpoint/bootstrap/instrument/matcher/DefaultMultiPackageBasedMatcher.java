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
import com.navercorp.pinpoint.common.util.Assert;

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
        Assert.requireNonNull(basePackageNames, "basePackageNames must not be null");
        final List<String> list = new ArrayList<String>();
        for (String basePackageName : basePackageNames) {
            // skip null and empty.
            if (basePackageName != null && !basePackageName.isEmpty()) {
                list.add(basePackageName);
            }
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("basePackageNames must not be empty " + basePackageNames);
        }
        this.basePackageNames = Collections.unmodifiableList(list);

        MatcherOperand operand = null;
        for (String basePackageName : this.basePackageNames) {
            final MatcherOperand packageMatcherOperand = new PackageInternalNameMatcherOperand(basePackageName);
            if (operand == null) {
                operand = packageMatcherOperand;
            } else {
                // package OR ...
                operand = operand.or(packageMatcherOperand);
            }
        }
        if (additional != null) {
            // (package OR ...) AND additional
            operand = operand.and(additional);
        }
        this.matcherOperand = operand;
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
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
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.OrMatcherOperator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class DefaultMultiPackageBasedMatcherTest {

    @Test
    public void getMatcherOperandWithMulitPackageName() {
        DefaultMultiPackageBasedMatcher matcher = new DefaultMultiPackageBasedMatcher(Arrays.asList("java", "javax"));
        assertThat(matcher.getBasePackageNames())
                .contains("java", "javax");

        MatcherOperand operand = matcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(OrMatcherOperator.class);
        OrMatcherOperator operator = (OrMatcherOperator) operand;
        assertThat(operator.getLeftOperand()).isInstanceOf(PackageInternalNameMatcherOperand.class);
        PackageInternalNameMatcherOperand leftOperand = (PackageInternalNameMatcherOperand) operator.getLeftOperand();
        assertEquals("java", leftOperand.getPackageInternalName());

        assertThat(operator.getRightOperand()).isInstanceOf(PackageInternalNameMatcherOperand.class);
        PackageInternalNameMatcherOperand rightOperand = (PackageInternalNameMatcherOperand) operator.getRightOperand();
        assertEquals("javax", rightOperand.getPackageInternalName());
    }
}
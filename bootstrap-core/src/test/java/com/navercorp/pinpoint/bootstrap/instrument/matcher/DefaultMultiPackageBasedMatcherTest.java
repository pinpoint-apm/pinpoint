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
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class DefaultMultiPackageBasedMatcherTest {

    @Test
    public void getMatcherOperandWithMulitPackageName() throws Exception {
        DefaultMultiPackageBasedMatcher matcher = new DefaultMultiPackageBasedMatcher(Arrays.asList("java", "javax"));
        assertTrue(matcher.getBasePackageNames().contains("java"));
        assertTrue(matcher.getBasePackageNames().contains("javax"));

        MatcherOperand operand = matcher.getMatcherOperand();
        assertTrue(operand instanceof OrMatcherOperator);
        OrMatcherOperator operator = (OrMatcherOperator) operand;
        assertTrue(operator.getLeftOperand() instanceof PackageInternalNameMatcherOperand);
        PackageInternalNameMatcherOperand leftOperand = (PackageInternalNameMatcherOperand) operator.getLeftOperand();
        assertEquals("java", leftOperand.getPackageInternalName());

        assertTrue(operator.getRightOperand() instanceof PackageInternalNameMatcherOperand);
        PackageInternalNameMatcherOperand rightOperand = (PackageInternalNameMatcherOperand) operator.getRightOperand();
        assertEquals("javax", rightOperand.getPackageInternalName());
    }
}
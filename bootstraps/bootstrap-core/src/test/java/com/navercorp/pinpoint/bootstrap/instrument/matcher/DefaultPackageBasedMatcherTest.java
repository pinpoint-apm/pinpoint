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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class DefaultPackageBasedMatcherTest {

    @Test
    public void getMatcherOperandWithPackageName() {
        DefaultPackageBasedMatcher packageBasedMatcher = new DefaultPackageBasedMatcher("java.lang");
        assertEquals("java.lang", packageBasedMatcher.getBasePackageName());

        MatcherOperand operand = packageBasedMatcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(PackageInternalNameMatcherOperand.class);
        PackageInternalNameMatcherOperand packageInternalNameMatcherOperand = (PackageInternalNameMatcherOperand) operand;
        assertEquals("java/lang", packageInternalNameMatcherOperand.getPackageInternalName());
    }

    @Test
    public void getMatcherOperandWithPackageNameAndAdditional() {
        InterfaceInternalNameMatcherOperand additional = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false);
        PackageBasedMatcher packageBasedMatcher = new DefaultPackageBasedMatcher("java.lang", additional);
        assertEquals("java.lang", packageBasedMatcher.getBasePackageName());

        MatcherOperand operand = packageBasedMatcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(AndMatcherOperator.class);

        AndMatcherOperator operator = (AndMatcherOperator) operand;
        assertThat(operator.getLeftOperand()).isInstanceOf(PackageInternalNameMatcherOperand.class);
        assertThat(operator.getRightOperand()).isInstanceOf(InterfaceInternalNameMatcherOperand.class);
    }

    @Test
    public void getMatcherOperandWithPackageNameAndAdditionalIsNull() {
        // check unusual pattern.
        PackageBasedMatcher classMatcher = new DefaultPackageBasedMatcher("java.lang", null);
        MatcherOperand operand = classMatcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(PackageInternalNameMatcherOperand.class);
        PackageInternalNameMatcherOperand annotationInternalNameMatcherOperand = (PackageInternalNameMatcherOperand) operand;
        assertEquals("java/lang", annotationInternalNameMatcherOperand.getPackageInternalName());
    }

    @Test
    public void getMatcherOperandWithPackageNameIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            final String packageName = null;
            PackageBasedMatcher matcher = new DefaultPackageBasedMatcher(packageName);
        });
    }

    @Test
    public void getMatcherOperandWithPackageNameIsEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            PackageBasedMatcher matcher = new DefaultPackageBasedMatcher("");
        });
    }
}
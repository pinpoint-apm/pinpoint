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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.AnnotationInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class DefaultPackageBasedMatcherTest {

    @Test
    public void getMatcherOperandWithPackageName() throws Exception {
        DefaultPackageBasedMatcher packageBasedMatcher = new DefaultPackageBasedMatcher("java.lang");
        assertEquals("java.lang", packageBasedMatcher.getBasePackageName());

        MatcherOperand operand = packageBasedMatcher.getMatcherOperand();
        assertTrue(operand instanceof PackageInternalNameMatcherOperand);
        PackageInternalNameMatcherOperand packageInternalNameMatcherOperand = (PackageInternalNameMatcherOperand) operand;
        assertTrue(packageInternalNameMatcherOperand.getPackageInternalName().equals("java/lang"));
    }

    @Test
    public void getMatcherOperandWithPackageNameAndAdditional() throws Exception {
        InterfaceInternalNameMatcherOperand additional = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false);
        PackageBasedMatcher packageBasedMatcher = new DefaultPackageBasedMatcher("java.lang", additional);
        assertEquals("java.lang", packageBasedMatcher.getBasePackageName());

        MatcherOperand operand = packageBasedMatcher.getMatcherOperand();
        assertTrue(operand instanceof AndMatcherOperator);

        AndMatcherOperator operator = (AndMatcherOperator) operand;
        assertTrue(operator.getLeftOperand() instanceof PackageInternalNameMatcherOperand);
        assertTrue(operator.getRightOperand() instanceof InterfaceInternalNameMatcherOperand);
    }

    @Test
    public void getMatcherOperandWithPackageNameAndAdditionalIsNull() throws Exception {
        // check unusual pattern.
        PackageBasedMatcher classMatcher = new DefaultPackageBasedMatcher("java.lang", null);
        MatcherOperand operand = classMatcher.getMatcherOperand();
        assertTrue(operand instanceof PackageInternalNameMatcherOperand);
        PackageInternalNameMatcherOperand annotationInternalNameMatcherOperand = (PackageInternalNameMatcherOperand) operand;
        assertTrue(annotationInternalNameMatcherOperand.getPackageInternalName().equals("java/lang"));
    }

    @Test(expected = NullPointerException.class)
    public void getMatcherOperandWithPackageNameIsNull() throws Exception {
        final String packageName = null;
        PackageBasedMatcher matcher = new DefaultPackageBasedMatcher(packageName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMatcherOperandWithPackageNameIsEmpty() throws Exception {
        PackageBasedMatcher matcher = new DefaultPackageBasedMatcher("");
    }
}
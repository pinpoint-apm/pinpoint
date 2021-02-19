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
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class DefaultClassBasedMatcherTest {

    @Test
    public void getMatcherOperandWithClassName() throws Exception {
        DefaultClassBasedMatcher matcher = new DefaultClassBasedMatcher("java.lang.String");
        assertEquals("java.lang.String", matcher.getBaseClassName());

        MatcherOperand operand = matcher.getMatcherOperand();
        assertTrue(operand instanceof ClassInternalNameMatcherOperand);
        ClassInternalNameMatcherOperand classInternalNameMatcherOperand = (ClassInternalNameMatcherOperand) operand;
        assertTrue(classInternalNameMatcherOperand.getClassInternalName().equals("java/lang/String"));
    }

    @Test
    public void getMatcherOperandWithClassNameAndAdditional() throws Exception {
        InterfaceInternalNameMatcherOperand additional = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false);
        DefaultClassBasedMatcher matcher = new DefaultClassBasedMatcher("java.lang.String", additional);
        assertEquals("java.lang.String", matcher.getBaseClassName());

        MatcherOperand operand = matcher.getMatcherOperand();
        assertTrue(operand instanceof AndMatcherOperator);

        AndMatcherOperator operator = (AndMatcherOperator) operand;
        assertTrue(operator.getLeftOperand() instanceof ClassInternalNameMatcherOperand);
        assertTrue(operator.getRightOperand() instanceof InterfaceInternalNameMatcherOperand);
    }

    @Test(expected = NullPointerException.class)
    public void getMatcherOperandWithClassNameIsNull() throws Exception {
        final String className = null;
        DefaultClassBasedMatcher matcher = new DefaultClassBasedMatcher(null);
    }

}
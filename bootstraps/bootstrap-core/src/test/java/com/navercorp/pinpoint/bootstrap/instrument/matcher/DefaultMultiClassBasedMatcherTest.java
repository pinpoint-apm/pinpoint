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
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.OrMatcherOperator;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class DefaultMultiClassBasedMatcherTest {

    @Test
    public void getMatcherOperandWithMultiClassName() throws Exception {
        // (class OR class)
        DefaultMultiClassBasedMatcher matcher = new DefaultMultiClassBasedMatcher(Arrays.asList("java.lang.String", "java.lang.Thread"));
        assertTrue(matcher.getBaseClassNames().contains("java.lang.String"));
        assertTrue(matcher.getBaseClassNames().contains("java.lang.Thread"));

        MatcherOperand operand = matcher.getMatcherOperand();
        assertTrue(operand instanceof OrMatcherOperator);
        OrMatcherOperator operator = (OrMatcherOperator) operand;
        assertTrue(operator.getLeftOperand() instanceof ClassInternalNameMatcherOperand);
        ClassInternalNameMatcherOperand leftOperand = (ClassInternalNameMatcherOperand) operator.getLeftOperand();
        assertEquals("java/lang/String", leftOperand.getClassInternalName());

        assertTrue(operator.getRightOperand() instanceof ClassInternalNameMatcherOperand);
        ClassInternalNameMatcherOperand rightOperand = (ClassInternalNameMatcherOperand) operator.getRightOperand();
        assertEquals("java/lang/Thread", rightOperand.getClassInternalName());
    }

    @Test
    public void getMatcherOperandWithMultiClassNameAndAdditional() throws Exception {
        // (class OR class) AND interface
        InterfaceInternalNameMatcherOperand additional = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false);
        DefaultMultiClassBasedMatcher matcher = new DefaultMultiClassBasedMatcher(Arrays.asList("java.lang.String", "java.lang.Thread"), additional);
        assertTrue(matcher.getBaseClassNames().contains("java.lang.String"));
        assertTrue(matcher.getBaseClassNames().contains("java.lang.Thread"));

        MatcherOperand operand = matcher.getMatcherOperand();
        assertTrue(operand instanceof AndMatcherOperator);
        AndMatcherOperator operator = (AndMatcherOperator) operand;
        // (class OR class)
        assertTrue(operator.getLeftOperand() instanceof OrMatcherOperator);
        // ... AND interface
        assertTrue(operator.getRightOperand() instanceof InterfaceInternalNameMatcherOperand);
        InterfaceInternalNameMatcherOperand rightOperand = (InterfaceInternalNameMatcherOperand) operator.getRightOperand();
        assertEquals("java/lang/Runnable", rightOperand.getInterfaceInternalName());
    }



}
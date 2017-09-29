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
package com.navercorp.pinpoint.bootstrap.instrument.matcher.operand;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.AndMatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.NotMatcherOperator;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operator.OrMatcherOperator;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class AbstractMatcherOperandTest {

    @Test
    public void and() {
        MatcherOperand operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.and(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false));
        operand = operand.and(new AnnotationInternalNameMatcherOperand("javax/annotation/Resource", false));

        assertTrue(operand instanceof AndMatcherOperator);
        AndMatcherOperator operator = (AndMatcherOperator) operand;
        assertTrue(operator.getLeftOperand() instanceof AndMatcherOperator);
        assertTrue(operator.getRightOperand() instanceof AnnotationInternalNameMatcherOperand);

        operator = (AndMatcherOperator) operator.getLeftOperand();
        assertTrue(operator.getLeftOperand() instanceof ClassInternalNameMatcherOperand);
        assertTrue(operator.getRightOperand() instanceof InterfaceInternalNameMatcherOperand);
    }

    @Test
    public void or() {
        MatcherOperand operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.or(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false));
        operand = operand.or(new AnnotationInternalNameMatcherOperand("javax/annotation/Resource", false));

        assertTrue(operand instanceof OrMatcherOperator);
        OrMatcherOperator operator = (OrMatcherOperator) operand;
        assertTrue(operator.getLeftOperand() instanceof OrMatcherOperator);
        assertTrue(operator.getRightOperand() instanceof AnnotationInternalNameMatcherOperand);

        operator = (OrMatcherOperator) operator.getLeftOperand();
        assertTrue(operator.getLeftOperand() instanceof ClassInternalNameMatcherOperand);
        assertTrue(operator.getRightOperand() instanceof InterfaceInternalNameMatcherOperand);
    }

    @Test
    public void not() {
        MatcherOperand operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.not();

        assertTrue(operand instanceof NotMatcherOperator);
        NotMatcherOperator operator = (NotMatcherOperator) operand;
        assertTrue(operator.getRightOperand() instanceof ClassInternalNameMatcherOperand);
    }
}
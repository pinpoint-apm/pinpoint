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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jaehong.kim
 */
public class AbstractMatcherOperandTest {

    @Test
    public void and() {
        MatcherOperand operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.and(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false));
        operand = operand.and(new AnnotationInternalNameMatcherOperand("javax/annotation/Resource", false));

        assertThat(operand).isInstanceOf(AndMatcherOperator.class);
        AndMatcherOperator operator = (AndMatcherOperator) operand;
        assertThat(operator.getLeftOperand()).isInstanceOf(AndMatcherOperator.class);
        assertThat(operator.getRightOperand()).isInstanceOf(AnnotationInternalNameMatcherOperand.class);

        operator = (AndMatcherOperator) operator.getLeftOperand();
        assertThat(operator.getLeftOperand()).isInstanceOf(ClassInternalNameMatcherOperand.class);
        assertThat(operator.getRightOperand()).isInstanceOf(InterfaceInternalNameMatcherOperand.class);
    }

    @Test
    public void or() {
        MatcherOperand operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.or(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false));
        operand = operand.or(new AnnotationInternalNameMatcherOperand("javax/annotation/Resource", false));

        assertThat(operand).isInstanceOf(OrMatcherOperator.class);
        OrMatcherOperator operator = (OrMatcherOperator) operand;
        assertThat(operator.getLeftOperand()).isInstanceOf(OrMatcherOperator.class);
        assertThat(operator.getRightOperand()).isInstanceOf(AnnotationInternalNameMatcherOperand.class);

        operator = (OrMatcherOperator) operator.getLeftOperand();
        assertThat(operator.getLeftOperand()).isInstanceOf(ClassInternalNameMatcherOperand.class);
        assertThat(operator.getRightOperand()).isInstanceOf(InterfaceInternalNameMatcherOperand.class);
    }

    @Test
    public void not() {
        MatcherOperand operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.not();

        assertThat(operand).isInstanceOf(NotMatcherOperator.class);
        NotMatcherOperator operator = (NotMatcherOperator) operand;
        assertThat(operator.getRightOperand()).isInstanceOf(ClassInternalNameMatcherOperand.class);
    }
}
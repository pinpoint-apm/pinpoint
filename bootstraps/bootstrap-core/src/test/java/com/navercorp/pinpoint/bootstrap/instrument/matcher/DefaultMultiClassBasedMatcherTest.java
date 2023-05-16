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
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class DefaultMultiClassBasedMatcherTest {

    @Test
    public void getMatcherOperandWithMultiClassName() {
        // (class OR class)
        DefaultMultiClassBasedMatcher matcher = new DefaultMultiClassBasedMatcher(Arrays.asList("java.lang.String", "java.lang.Thread"));
        assertThat(matcher.getBaseClassNames())
                .contains("java.lang.String", "java.lang.Thread");

        MatcherOperand operand = matcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(OrMatcherOperator.class);
        OrMatcherOperator operator = (OrMatcherOperator) operand;
        assertThat(operator.getLeftOperand()).isInstanceOf(ClassInternalNameMatcherOperand.class);
        ClassInternalNameMatcherOperand leftOperand = (ClassInternalNameMatcherOperand) operator.getLeftOperand();
        assertEquals("java/lang/String", leftOperand.getClassInternalName());

        assertThat(operator.getRightOperand()).isInstanceOf(ClassInternalNameMatcherOperand.class);
        ClassInternalNameMatcherOperand rightOperand = (ClassInternalNameMatcherOperand) operator.getRightOperand();
        assertEquals("java/lang/Thread", rightOperand.getClassInternalName());
    }

    @Test
    public void getMatcherOperandWithMultiClassNameAndAdditional() {
        // (class OR class) AND interface
        InterfaceInternalNameMatcherOperand additional = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false);
        DefaultMultiClassBasedMatcher matcher = new DefaultMultiClassBasedMatcher(Arrays.asList("java.lang.String", "java.lang.Thread"), additional);
        assertThat(matcher.getBaseClassNames())
                .contains("java.lang.String", "java.lang.Thread");

        MatcherOperand operand = matcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(AndMatcherOperator.class);
        AndMatcherOperator operator = (AndMatcherOperator) operand;
        // (class OR class)
        assertThat(operator.getLeftOperand()).isInstanceOf(OrMatcherOperator.class);
        // ... AND interface
        assertThat(operator.getRightOperand()).isInstanceOf(InterfaceInternalNameMatcherOperand.class);
        InterfaceInternalNameMatcherOperand rightOperand = (InterfaceInternalNameMatcherOperand) operator.getRightOperand();
        assertEquals("java/lang/Runnable", rightOperand.getInterfaceInternalName());
    }


}
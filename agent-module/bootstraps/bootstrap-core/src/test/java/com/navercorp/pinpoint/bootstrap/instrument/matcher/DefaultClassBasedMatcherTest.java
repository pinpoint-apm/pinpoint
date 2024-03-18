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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class DefaultClassBasedMatcherTest {

    @Test
    public void getMatcherOperandWithClassName() {
        DefaultClassBasedMatcher matcher = new DefaultClassBasedMatcher("java.lang.String");
        assertEquals("java.lang.String", matcher.getBaseClassName());

        MatcherOperand operand = matcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(ClassInternalNameMatcherOperand.class);
        ClassInternalNameMatcherOperand classInternalNameMatcherOperand = (ClassInternalNameMatcherOperand) operand;
        assertEquals("java/lang/String", classInternalNameMatcherOperand.getClassInternalName());
    }

    @Test
    public void getMatcherOperandWithClassNameAndAdditional() {
        InterfaceInternalNameMatcherOperand additional = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false);
        DefaultClassBasedMatcher matcher = new DefaultClassBasedMatcher("java.lang.String", additional);
        assertEquals("java.lang.String", matcher.getBaseClassName());

        MatcherOperand operand = matcher.getMatcherOperand();
        assertThat(operand).isInstanceOf(AndMatcherOperator.class);

        AndMatcherOperator operator = (AndMatcherOperator) operand;
        assertThat(operator.getLeftOperand()).isInstanceOf(ClassInternalNameMatcherOperand.class);
        assertThat(operator.getRightOperand()).isInstanceOf(InterfaceInternalNameMatcherOperand.class);
    }

    @Test
    public void getMatcherOperandWithClassNameIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            final String className = null;
            DefaultClassBasedMatcher matcher = new DefaultClassBasedMatcher(null);
        });
    }

}
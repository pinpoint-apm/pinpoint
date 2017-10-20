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
package com.navercorp.pinpoint.bootstrap.instrument.matcher.operator;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class AndMatcherOperatorTest {

    @Test
    public void base() throws Exception {
        AndMatcherOperator operator = new AndMatcherOperator(new ClassInternalNameMatcherOperand("java/lang/String"), new InterfaceInternalNameMatcherOperand("java/lang/Serializable", false));
        assertEquals(2, operator.getPrecedence());
        assertTrue(operator.isOperator());
        assertFalse(operator.isIndex());
        assertTrue(operator.getLeftOperand() instanceof ClassInternalNameMatcherOperand);
        assertTrue(operator.getRightOperand() instanceof InterfaceInternalNameMatcherOperand);
        assertEquals(3, operator.getExecutionCost());
    }
}
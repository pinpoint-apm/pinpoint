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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class NotMatcherOperatorTest {

    @Test
    public void base() throws Exception {
        NotMatcherOperator operator = new NotMatcherOperator(new InterfaceInternalNameMatcherOperand("java/lang/Serializable", false));
        assertEquals(3, operator.getPrecedence());
        assertTrue(operator.isOperator());
        assertFalse(operator.isIndex());
        assertEquals(2, operator.getExecutionCost());
    }
}
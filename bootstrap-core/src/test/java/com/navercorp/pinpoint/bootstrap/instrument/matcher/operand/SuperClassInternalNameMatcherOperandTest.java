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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class SuperClassInternalNameMatcherOperandTest {

    @Test
    public void base() throws Exception {
        SuperClassInternalNameMatcherOperand operand = new SuperClassInternalNameMatcherOperand("java/lang/Object", true);
        assertTrue(operand.isConsiderHierarchy());
        assertTrue(operand.isJavaPackage());
        assertFalse(operand.isOperator());
        assertFalse(operand.isIndex());
        assertEquals(5, operand.getExecutionCost());
        assertEquals("java/lang/Object", operand.getSuperClassInternalNames());

        assertTrue(operand.match("java/lang/Object"));
        assertFalse(operand.match("java/lang/String"));
    }
}
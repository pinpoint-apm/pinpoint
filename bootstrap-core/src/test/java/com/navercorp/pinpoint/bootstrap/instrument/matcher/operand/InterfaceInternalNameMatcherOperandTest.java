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

import com.navercorp.pinpoint.common.util.ClassUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class InterfaceInternalNameMatcherOperandTest {

    @Test
    public void base() throws Exception {
        InterfaceInternalNameMatcherOperand operand = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", true);
        assertEquals(5, operand.getExecutionCost());
        assertTrue(operand.isJavaPackage());
        assertFalse(operand.isIndex());
        assertFalse(operand.isOperator());
        assertTrue(operand.isConsiderHierarchy());

        assertTrue(operand.match("java/lang/Runnable"));
        assertFalse(operand.match("java/lang/Comparable"));

        assertEquals("java/lang/Runnable", operand.getInterfaceInternalName());


        String className = Dummy.class.getName();
        operand = new InterfaceInternalNameMatcherOperand(className, false);
        assertEquals(2, operand.getExecutionCost());
        assertFalse(operand.isJavaPackage());
        assertFalse(operand.isIndex());
        assertFalse(operand.isOperator());
        assertFalse(operand.isConsiderHierarchy());

        assertTrue(operand.match(ClassUtils.toInternalName(className)));
        assertFalse(operand.match(className));

        assertEquals(ClassUtils.toInternalName(className), operand.getInterfaceInternalName());
    }

    interface Dummy {
    }
}
/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.AnnotationInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class TransformerMatcherExecutionPlannerTest {

    @Test
    public void findIndex() throws Exception {
        TransformerMatcherExecutionPlanner executionPlanner = new TransformerMatcherExecutionPlanner();

        // and
        MatcherOperand operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.and(new InterfaceInternalNameMatcherOperand("java/lang/Comparable", false));
        operand = operand.and(new AnnotationInternalNameMatcherOperand("java/lang/Override", false));
        operand = operand.and(new PackageInternalNameMatcherOperand("java/lang"));

        List<MatcherOperand> result = executionPlanner.findIndex(operand);
        assertEquals(1, result.size());

        // or
        operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.or(new InterfaceInternalNameMatcherOperand("java/lang/Comparable", false));
        operand = operand.or(new AnnotationInternalNameMatcherOperand("java/lang/Override", false));
        operand = operand.or(new PackageInternalNameMatcherOperand("java/lang"));

        result = executionPlanner.findIndex(operand);
        assertEquals(2, result.size());

        // not
        operand = new InterfaceInternalNameMatcherOperand("java/lang/Comparable", false);
        operand = operand.or(new AnnotationInternalNameMatcherOperand("java/lang/Override", false));
        operand = operand.and(new PackageInternalNameMatcherOperand("javax").not());

        result = executionPlanner.findIndex(operand);
        assertEquals(0, result.size());

        // none
        operand = new InterfaceInternalNameMatcherOperand("java/lang/Comparable", false);
        operand = operand.or(new AnnotationInternalNameMatcherOperand("java/lang/Override", false));
        operand = operand.and(new SuperClassInternalNameMatcherOperand("java/lang/Object", true));

        result = executionPlanner.findIndex(operand);
        assertEquals(0, result.size());
    }
}
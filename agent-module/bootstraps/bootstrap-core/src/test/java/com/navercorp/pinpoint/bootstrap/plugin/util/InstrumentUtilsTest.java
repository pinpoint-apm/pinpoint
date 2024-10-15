/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.util;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

class InstrumentUtilsTest {

    @Test
    void findMethod() {
        String methodName = "findMethod";

        InstrumentClass mockClass = mock(InstrumentClass.class);
        InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockClass.getDeclaredMethod(methodName)).thenReturn(mockMethod);

        try {
            InstrumentUtils.findMethod(mockClass, methodName);
        } catch (NotFoundInstrumentException e) {
            Assertions.fail("not found " + methodName);
        }

        try {
            InstrumentUtils.findMethod(mockClass, "unknownMethod");
            Assertions.fail("found " + methodName);
        } catch (NotFoundInstrumentException e) {
        }
    }

    @Test
    void findMethodOrIgnore() {
        String methodName = "findMethod";

        InstrumentClass mockClass = mock(InstrumentClass.class);
        InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockClass.getDeclaredMethod(methodName)).thenReturn(mockMethod);

        try {
            InstrumentMethod instrumentMethod = InstrumentUtils.findMethodOrIgnore(mockClass, methodName);
            Assertions.assertNotNull(instrumentMethod);
        } catch (NotFoundInstrumentException e) {
            Assertions.fail("throw exception");
        }

        try {
            InstrumentMethod instrumentMethod = InstrumentUtils.findMethodOrIgnore(mockClass, "unknownMethod");
            Assertions.assertNotNull(instrumentMethod);
        } catch (NotFoundInstrumentException e) {
            Assertions.fail("throw exception");
        }
    }

    @Test
    void findConstructor() {
        InstrumentClass mockClass = mock(InstrumentClass.class);
        InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockClass.getConstructor()).thenReturn(mockMethod);

        try {
            InstrumentUtils.findConstructor(mockClass);
        } catch (NotFoundInstrumentException e) {
            Assertions.fail("not found constructor");
        }

        try {
            InstrumentUtils.findConstructor(mockClass, "unknownParameter");
            Assertions.fail("found constructor");
        } catch (NotFoundInstrumentException e) {
        }
    }

    @Test
    void findConstructorOrIgnore() {
        InstrumentClass mockClass = mock(InstrumentClass.class);
        InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockClass.getConstructor()).thenReturn(mockMethod);

        try {
            InstrumentMethod instrumentMethod = InstrumentUtils.findConstructorOrIgnore(mockClass);
            Assertions.assertNotNull(instrumentMethod);
        } catch (NotFoundInstrumentException e) {
            Assertions.fail("throw exception");
        }

        try {
            InstrumentMethod instrumentMethod = InstrumentUtils.findConstructorOrIgnore(mockClass, "unknownParameter");
            Assertions.assertNotNull(instrumentMethod);
        } catch (NotFoundInstrumentException e) {
            Assertions.fail("throw exception");
        }
    }
}
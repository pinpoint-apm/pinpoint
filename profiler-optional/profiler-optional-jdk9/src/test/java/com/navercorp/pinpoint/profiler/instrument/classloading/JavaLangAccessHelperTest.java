/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.classloading;

import org.junit.Test;

import java.lang.invoke.MethodHandle;

import static org.junit.Assert.assertNotNull;

/**
 * @author jaehong.kim
 */
public class JavaLangAccessHelperTest {

    @Test
    public void getJavaLangAccessObject() {
        Object object = JavaLangAccessHelper.getJavaLangAccessObject();
        assertNotNull(object);
        System.out.println(object);
    }

    @Test
    public void getRegisterShutdownHookMethodHandle() {
        MethodHandle methodHandle = JavaLangAccessHelper.getRegisterShutdownHookMethodHandle();
        assertNotNull(methodHandle);
    }

    @Test
    public void getDefineClassMethodHandle() {
        MethodHandle methodHandle = JavaLangAccessHelper.getDefineClassMethodHandle();
        assertNotNull(methodHandle);
    }
}
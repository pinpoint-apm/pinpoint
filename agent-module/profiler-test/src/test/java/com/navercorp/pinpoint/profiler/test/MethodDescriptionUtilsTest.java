/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MethodDescriptionUtilsTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void toMethodDescriptor() {
        String result = MethodDescriptionUtils.toJavaMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.setInner(com.navercorp.test.pinpoint.plugin.spring.beans.Inner Inner):23");
        assertEquals("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.setInner(com.navercorp.test.pinpoint.plugin.spring.beans.Inner)", result);

        String desc = MethodDescriptionUtils.toJavaMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer", "setInner", new String[]{"com.navercorp.test.pinpoint.plugin.spring.beans.Inner"});
        assertEquals(result, desc);
    }

    @Test
    public void toMethodDescriptor_multi_argument() {
        String result = MethodDescriptionUtils.toJavaMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.setInner(com.navercorp.test.pinpoint.plugin.spring.beans.Inner Inner, java.lang.String String):23");
        assertEquals("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.setInner(com.navercorp.test.pinpoint.plugin.spring.beans.Inner, java.lang.String)", result);

        String desc = MethodDescriptionUtils.toJavaMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer", "setInner", new String[]{"com.navercorp.test.pinpoint.plugin.spring.beans.Inner", "java.lang.String"});
        assertEquals(result, desc);
    }

    @Test
    public void toMethodDescriptor_constructor() {

        String result = MethodDescriptionUtils.toJavaMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.Outer(com.navercorp.test.pinpoint.plugin.spring.beans.Inner Inner):23");
        assertEquals("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.Outer(com.navercorp.test.pinpoint.plugin.spring.beans.Inner)", result);

        String desc = MethodDescriptionUtils.toJavaMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer", "Outer", new String[]{"com.navercorp.test.pinpoint.plugin.spring.beans.Inner"});
        assertEquals(result, desc);
    }


    @Test
    public void testGetConstructorSimpleName() throws Exception {

        Class<String> stringClass = String.class;
        Constructor<String> constructor = stringClass.getConstructor();
        logger.debug("{}", constructor.getName());

        assertEquals(MethodDescriptionUtils.getConstructorSimpleName(constructor), "String");

    }

    @Test
    public void testGetConstructorSimpleName_no_package() {

        assertEquals(MethodDescriptionUtils.getConstructorSimpleName("String"), "String");
    }
}
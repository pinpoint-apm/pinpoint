/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class ApiUtilsTest {

    @Test
    public void toMethodDescriptor() {
        String result = ApiUtils.toMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.setInner(com.navercorp.test.pinpoint.plugin.spring.beans.Inner Inner):23");
        assertEquals("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.setInner(com.navercorp.test.pinpoint.plugin.spring.beans.Inner)", result);

        String desc = ApiUtils.toMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer", "setInner", new String[]{"com.navercorp.test.pinpoint.plugin.spring.beans.Inner"});
        assertEquals(result, desc);
    }

    @Test
    public void toMethodDescriptor_multi_argument() {
        String result = ApiUtils.toMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.setInner(com.navercorp.test.pinpoint.plugin.spring.beans.Inner Inner, java.lang.String String):23");
        assertEquals("com.navercorp.test.pinpoint.plugin.spring.beans.Outer.setInner(com.navercorp.test.pinpoint.plugin.spring.beans.Inner, java.lang.String)", result);

        String desc = ApiUtils.toMethodDescriptor("com.navercorp.test.pinpoint.plugin.spring.beans.Outer", "setInner", new String[]{"com.navercorp.test.pinpoint.plugin.spring.beans.Inner", "java.lang.String"});
        assertEquals(result, desc);
    }
}
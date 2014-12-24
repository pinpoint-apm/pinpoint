/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.util;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.util.MetaObject;

/**
 * @author emeroad
 */
public class MetaObjectTest {
    private String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    @Test
    public void testSetInvoke() throws Exception {
        MetaObjectTest test = new MetaObjectTest();

        MetaObject metaObject = new MetaObject("setTest", String.class);
        Object result = metaObject.invoke(test, "set");
        Assert.assertEquals(test.getTest(), "set");
        Assert.assertNull(result);
    }

    @Test
    public void testGetInvoke() throws Exception {
        MetaObjectTest test = new MetaObjectTest();
        test.setTest("get");

        MetaObject metaObject = new MetaObject("getTest");
        Object result = metaObject.invoke(test);
        Assert.assertEquals(test.getTest(), result);
    }
}

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

package com.navercorp.pinpoint.profiler.modifier.db.interceptor;

import org.junit.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.profiler.modifier.db.interceptor.BindValueUtils;

public class BindValueUtilsTest {

    @Test
    public void testBindValueToString() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue);
        Assert.assertEquals("a, b", result);
    }

    @Test
    public void testBindValueToString_limit1() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, 0);
        Assert.assertEquals("...(2)", result);
    }

    @Test
    public void testBindValueToString_limit2() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, 1);
        Assert.assertEquals("a, ...(2)", result);
    }

    @Test
    public void testBindValueToString_limit3() throws Exception {
        String[] bindValue = {"abc", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, 1);
        Assert.assertEquals("a...(3), ...(2)", result);
    }

    @Test
    public void testBindValueToString_limit4() throws Exception {
        String[] bindValue = {"abc", "b", "c"};
        String result = BindValueUtils.bindValueToString(bindValue, 1);
        Assert.assertEquals("a...(3), ...(3)", result);
    }


    @Test
    public void testBindValueToString_limit5() throws Exception {
        String[] bindValue = {"abc", "b", "c"};
        String result = BindValueUtils.bindValueToString(bindValue, 1024);
        Assert.assertEquals("abc, b, c", result);
    }

    @Test
    public void testBindValueToString_limit6() throws Exception {
        String[] bindValue = {"a", "b", "1234567891012"};
        // limit is smaller than 3rd arg.
        String result = BindValueUtils.bindValueToString(bindValue, 10);
        Assert.assertEquals("a, b, 1234567891...(13)", result);
    }

    @Test
    public void testBindValueToString_limit7() throws Exception {
        String[] bindValue = {"a", "12345678901", "c"};
        // limit is smaller than 2nd arg.
        String result = BindValueUtils.bindValueToString(bindValue, 10);
        Assert.assertEquals("a, 1234567890...(11), ...(3)", result);
    }

    @Test
    public void testBindValueToString_null() throws Exception {
        String result = BindValueUtils.bindValueToString(null, 10);
        Assert.assertEquals("", result);
    }

    @Test
    public void testBindValueToString_native() throws Exception {
        String[] bindValue = {"a", "b"};
        String result = BindValueUtils.bindValueToString(bindValue, -1);
        Assert.assertEquals("...(2)", result);
    }

    @Test
    public void testBindValueToString_singleLargeString() throws Exception {
        String[] bindValue = {"123456"};
        String result = BindValueUtils.bindValueToString(bindValue, 5);
        Assert.assertEquals("12345...(6)", result);
    }

    @Test
    public void testBindValueToString_twoLargeString() throws Exception {
        String[] bindValue = {"123456", "123456"};
        String result = BindValueUtils.bindValueToString(bindValue, 5);
        Assert.assertEquals("12345...(6), ...(2)", result);
    }
}
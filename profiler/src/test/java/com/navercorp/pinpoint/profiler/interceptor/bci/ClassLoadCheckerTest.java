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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClassLoadCheckerTest {

    @Test
    public void testExist() throws Exception {
        ClassLoadChecker checker = new ClassLoadChecker();
        boolean non = checker.exist(ClassLoader.getSystemClassLoader(), "test");
        Assert.assertFalse(non);

        boolean exist = checker.exist(ClassLoader.getSystemClassLoader(), "test");
        Assert.assertTrue(exist);
    }


    @Test(expected = NullPointerException.class)
    public void testNull() throws Exception {
        ClassLoadChecker checker = new ClassLoadChecker();
        checker.exist(null, "test");
    }
}
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

        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        ClassLoadChecker checker = new ClassLoadChecker();
        boolean non1 = checker.exist(systemClassLoader, "1");
        Assert.assertFalse(non1);

        boolean exist1 = checker.exist(systemClassLoader, "1");
        Assert.assertTrue(exist1);


        boolean non2 = checker.exist(systemClassLoader, "2");
        Assert.assertFalse(non2);

        boolean exist2 = checker.exist(systemClassLoader, "2");
        Assert.assertTrue(exist2);

    }


    @Test(expected = NullPointerException.class)
    public void testNull() throws Exception {
        ClassLoadChecker checker = new ClassLoadChecker();
        checker.exist(null, "test");
    }
}
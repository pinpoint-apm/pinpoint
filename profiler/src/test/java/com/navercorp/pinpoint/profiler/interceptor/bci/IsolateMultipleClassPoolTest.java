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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.junit.Test;

import static org.junit.Assert.*;

public class IsolateMultipleClassPoolTest {

    @Test
    public void testGetClassPool() throws Exception {

    }

    @Test
    public void testValues() throws Exception {
//        ClassPool pool = new ClassPool();
//        pool.get(this.getClass().getName());

        ClassPool pool2 = new ClassPool(true);
        pool2.get(this.getClass().getName());

//        ClassPool pool3 = new ClassPool(true);
//        pool3.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));

    }
}
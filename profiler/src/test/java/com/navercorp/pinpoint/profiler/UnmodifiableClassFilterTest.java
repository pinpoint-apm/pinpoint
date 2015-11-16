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

package com.navercorp.pinpoint.profiler;

import org.junit.Assert;

import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;


public class UnmodifiableClassFilterTest {

    @Test
    public void testDoFilter_Package() throws Exception {
        ClassFileFilter filter = new UnmodifiableClassFilter();

        Assert.assertSame(filter.accept(null, "java/test", null, null, null), ClassFileFilter.SKIP);
        Assert.assertSame(filter.accept(null, "javax/test", null, null, null), ClassFileFilter.SKIP);


        Assert.assertSame(filter.accept(null, "com/navercorp/pinpoint/", null, null, null), ClassFileFilter.CONTINUE);

        Assert.assertSame(filter.accept(null, "test", null, null, null), ClassFileFilter.CONTINUE);
    }


    @Test
    public void testDoFilter_ClassLoader() throws Exception {
        ClassFileFilter filter = new UnmodifiableClassFilter();

        Assert.assertSame(filter.accept(this.getClass().getClassLoader(), "test", null, null, null), ClassFileFilter.CONTINUE);

        URLClassLoader classLoader = new URLClassLoader(new URL[]{});
        Assert.assertSame(filter.accept(classLoader, "test", null, null, null), ClassFileFilter.CONTINUE);
    }
}
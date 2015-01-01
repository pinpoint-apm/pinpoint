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

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.profiler.ClassFileFilter;
import com.navercorp.pinpoint.profiler.DefaultClassFileFilter;

import java.net.URL;
import java.net.URLClassLoader;


public class DefaultClassFileFilterTest {

    @Test
    public void testDoFilter_Package() throws Exception {
        ClassFileFilter filter = new DefaultClassFileFilter(this.getClass().getClassLoader());

        Assert.assertTrue(filter.doFilter(null, "java/test", null, null, null));
        Assert.assertTrue(filter.doFilter(null, "javax/test", null, null, null));
        Assert.assertTrue(filter.doFilter(null, "com/navercorp/pinpoint/", null, null, null));

        Assert.assertFalse(filter.doFilter(null, "test", null, null, null));
    }


    @Test
    public void testDoFilter_ClassLoader() throws Exception {
        ClassFileFilter filter = new DefaultClassFileFilter(this.getClass().getClassLoader());


        Assert.assertTrue(filter.doFilter(this.getClass().getClassLoader(), "test", null, null, null));

        URLClassLoader classLoader = new URLClassLoader(new URL[]{});
        Assert.assertFalse(filter.doFilter(classLoader, "test", null, null, null));
    }
}
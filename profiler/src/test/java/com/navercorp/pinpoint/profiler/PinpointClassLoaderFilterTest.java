/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class PinpointClassLoaderFilterTest {

    @Test
    public void testDoFilter_ClassLoader() throws Exception {

        final URLClassLoader agentClassLoader = new URLClassLoader(new URL[0]);
        ClassFileFilter filter = new PinpointClassLoaderFilter(agentClassLoader);

        Assert.assertSame("bootstrap test", filter.accept(null, "test", null, null, null), ClassFileFilter.CONTINUE);

        final ClassLoader defaultClassLoader = ClassLoaderUtils.getDefaultClassLoader();
        Assert.assertSame(filter.accept(defaultClassLoader, "test", null, null, null), ClassFileFilter.CONTINUE);

        Assert.assertSame(filter.accept(agentClassLoader, "test", null, null, null), ClassFileFilter.SKIP);
    }
}
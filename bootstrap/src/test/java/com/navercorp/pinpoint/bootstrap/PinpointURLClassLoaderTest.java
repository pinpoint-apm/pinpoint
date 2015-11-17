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

package com.navercorp.pinpoint.bootstrap;

import org.junit.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.PinpointURLClassLoader;

import java.net.URL;

/**
 * @author emeroad
 */
public class PinpointURLClassLoaderTest {

    @Test
    public void testOnLoadClass() throws Exception {

        PinpointURLClassLoader cl = new PinpointURLClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader());
        try {
            cl.loadClass("test");
            Assert.fail();
        } catch (ClassNotFoundException ignored) {
        }

//        try {
//            cl.loadClass("com.navercorp.pinpoint.profiler.DefaultAgent");
//        } catch (ClassNotFoundException e) {
//
//        }
        // should be able to test using the above code, but it is not possible from bootstrap testcase.
        // it could be possible by specifying the full path to the URL classloader, but it would be harder to maintain.
        // for now, just test if DefaultAgent is specified to be loaded
        Assert.assertTrue(cl.onLoadClass("com.navercorp.pinpoint.profiler.DefaultAgent"));
    }
}

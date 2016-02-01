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

package com.navercorp.pinpoint.profiler.plugin;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassNameFilterChainTest {

    @Test
    public void testAccept() throws Exception {
        PluginPackageFilter include = new PluginPackageFilter(Arrays.asList("com.include"));
        PinpointProfilerPackageSkipFilter exclude = new PinpointProfilerPackageSkipFilter(Arrays.asList("com.exclude"));

        ClassNameFilterChain chain = new ClassNameFilterChain(Arrays.asList(include, exclude));


        Assert.assertTrue(chain.accept("com.include"));
        Assert.assertFalse(chain.accept("com.exclude"));

        Assert.assertFalse(chain.accept("unknown"));
    }
}
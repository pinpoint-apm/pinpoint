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
public class PluginPackageFilterTest {

    @Test
    public void testAccept() throws Exception {
        PluginPackageFilter filter = new PluginPackageFilter(Arrays.asList("com.plugin"));

        Assert.assertTrue(filter.accept("com.plugin.test.module"));

        Assert.assertFalse(filter.accept("test"));
    }
}
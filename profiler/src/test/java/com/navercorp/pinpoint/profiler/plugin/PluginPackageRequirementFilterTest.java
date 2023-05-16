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

import com.navercorp.pinpoint.common.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class PluginPackageRequirementFilterTest {

    @Test
    public void testAccept() {

        String testRequirements = "com.plugin.acceptTrue:java.util.List, com.plugin.acceptFalse:class.not.exist.hopefully";
        List<String> packageRequirementString = StringUtils.tokenizeToStringList(testRequirements, ",");
        ClassNameFilter filter = new PluginPackageClassRequirementFilter(packageRequirementString);

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        Assertions.assertTrue(filter.accept("com.plugin.acceptTrue.SomeClass", classLoader));
        Assertions.assertFalse(filter.accept("com.plugin.acceptFalse.SomeClass", classLoader));

        Assertions.assertTrue(filter.accept("com.notPlugin.SomeClass", classLoader));
    }

    @Test
    public void testNullRequirement() {
        List<String> packageRequirementString = StringUtils.tokenizeToStringList(null, ",");
        ClassNameFilter filter = new PluginPackageClassRequirementFilter(packageRequirementString);

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        Assertions.assertTrue(filter.accept("com.plugin.SomeClass", classLoader));
        Assertions.assertTrue(filter.accept("com.notPlugin.SomeClass", classLoader));
    }

    @Test
    public void testNoRequirement() {
        List<String> packageRequirementString = StringUtils.tokenizeToStringList("", ",");
        ClassNameFilter filter = new PluginPackageClassRequirementFilter(packageRequirementString);

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        Assertions.assertTrue(filter.accept("com.plugin.SomeClass", classLoader));
        Assertions.assertTrue(filter.accept("com.notPlugin.SomeClass", classLoader));
    }

    @Test
    public void testEmptyRequirement() {
        List<String> packageRequirementString = Collections.emptyList();
        ClassNameFilter filter = new PluginPackageClassRequirementFilter(packageRequirementString);

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        Assertions.assertTrue(filter.accept("com.plugin.SomeClass", classLoader));
        Assertions.assertTrue(filter.accept("com.notPlugin.SomeClass", classLoader));
    }

    @Test
    public void testNullClassloaderRequirement() {
        List<String> packageRequirementString = Collections.emptyList();
        ClassNameFilter filter = new PluginPackageClassRequirementFilter(packageRequirementString);

        Assertions.assertThrows(NullPointerException.class, () -> {
            filter.accept("com.plugin.SomeClass", null);
        });
        Assertions.assertThrows(NullPointerException.class, () -> {
            filter.accept("com.notPlugin.SomeClass", null);
        });
    }
}
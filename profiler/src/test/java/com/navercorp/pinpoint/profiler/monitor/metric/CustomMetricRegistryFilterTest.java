/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCount;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCount;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricRegistryFilter;
import com.navercorp.pinpoint.profiler.context.monitor.metric.DefaultCustomMetricRegistryFilter;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class CustomMetricRegistryFilterTest {

    @Test
    public void customMetricRegistryFilterTest() {
        final String allowedMetricName = "test";

        final DefaultCustomMetricRegistryFilter.AllowedSource allowedSource = new DefaultCustomMetricRegistryFilter.AllowedSource(allowedMetricName, IntCount.class);
        final CustomMetricRegistryFilter filter = new DefaultCustomMetricRegistryFilter(Arrays.asList(allowedSource));

        TestIntCount testIntCount = new TestIntCount(allowedMetricName);
        boolean filtered = filter.filter(testIntCount);
        Assert.assertFalse(filtered);

        TestIntCount testIntCount2 = new TestIntCount("test2");
        filtered = filter.filter(testIntCount2);
        Assert.assertTrue(filtered);


        TestLongCount testLongCount = new TestLongCount(allowedMetricName);
        filtered = filter.filter(testLongCount);
        Assert.assertTrue(filtered);
    }

    private static class TestIntCount implements IntCount {

        private final String name;

        public TestIntCount(String name) {
            this.name = name;
        }

        @Override
        public int getValue() {
            return 0;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    private static class TestLongCount implements LongCount {

        private final String name;

        public TestLongCount(String name) {
            this.name = name;
        }

        @Override
        public long getValue() {
            return 0;
        }

        @Override
        public String getName() {
            return name;
        }

    }

}

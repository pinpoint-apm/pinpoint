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

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
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

        final DefaultCustomMetricRegistryFilter.AllowedSource allowedSource = new DefaultCustomMetricRegistryFilter.AllowedSource(allowedMetricName, IntCounter.class);
        final CustomMetricRegistryFilter filter = new DefaultCustomMetricRegistryFilter(Arrays.asList(allowedSource));

        TestIntCounter testIntCount = new TestIntCounter(allowedMetricName);
        boolean filtered = filter.filter(testIntCount);
        Assert.assertFalse(filtered);

        TestIntCounter testIntCount2 = new TestIntCounter("test2");
        filtered = filter.filter(testIntCount2);
        Assert.assertTrue(filtered);


        TestLongCounter testLongCount = new TestLongCounter(allowedMetricName);
        filtered = filter.filter(testLongCount);
        Assert.assertTrue(filtered);
    }

    private static class TestIntCounter implements IntCounter {

        private final String name;

        public TestIntCounter(String name) {
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

    private static class TestLongCounter implements LongCounter {

        private final String name;

        public TestLongCounter(String name) {
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

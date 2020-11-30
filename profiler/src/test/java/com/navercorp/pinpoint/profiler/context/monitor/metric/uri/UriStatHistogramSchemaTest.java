/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.monitor.metric.uri;

import com.navercorp.pinpoint.profiler.monitor.metric.uri.UriStatHistogramSchema;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @author Taejin Koo
 */
public class UriStatHistogramSchemaTest {

    @Test
    public void uriStatHistogramSchemaTest() {
        Random random = new Random(System.currentTimeMillis());

        assertScheme(random, null, UriStatHistogramSchema.VERY_FAST, 0);
        assertScheme(random, UriStatHistogramSchema.VERY_FAST, UriStatHistogramSchema.FAST_1, 1);
        assertScheme(random, UriStatHistogramSchema.FAST_1, UriStatHistogramSchema.FAST_2, 2);
        assertScheme(random, UriStatHistogramSchema.FAST_2, UriStatHistogramSchema.FAST_3, 3);
        assertScheme(random, UriStatHistogramSchema.FAST_3, UriStatHistogramSchema.NORMAL_1, 4);
        assertScheme(random, UriStatHistogramSchema.NORMAL_1, UriStatHistogramSchema.NORMAL_2, 5);
        assertScheme(random, UriStatHistogramSchema.NORMAL_2, UriStatHistogramSchema.NORMAL_3, 6);
        assertScheme(random, UriStatHistogramSchema.NORMAL_3, UriStatHistogramSchema.SLOW_1, 7);
        assertScheme(random, UriStatHistogramSchema.SLOW_1, UriStatHistogramSchema.SLOW_2, 8);
        assertScheme(random, UriStatHistogramSchema.SLOW_2, UriStatHistogramSchema.VERY_SLOW, 9);
    }

    void assertScheme(Random random, UriStatHistogramSchema prev, UriStatHistogramSchema current, int expectedIndex) {
        long value = Math.abs(random.nextLong());
        value %= (current.getTo() - current.getFrom());

        if (prev != null) {
            value += prev.getTo();
        }

        UriStatHistogramSchema result = UriStatHistogramSchema.getValue(value);

        Assert.assertEquals(current, result);
        Assert.assertEquals(expectedIndex, result.getIndex());
    }

}

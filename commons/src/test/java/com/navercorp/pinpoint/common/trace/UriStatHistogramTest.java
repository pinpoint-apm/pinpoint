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

package com.navercorp.pinpoint.common.trace;

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @author Taejin Koo
 */
public class UriStatHistogramTest {

    @Test
    public void uriStatHistogramTest() {
        Random random = new Random(System.currentTimeMillis());

        assertScheme(random, null, UriStatHistogramBucket.UNDER_100, 0);
        assertScheme(random, UriStatHistogramBucket.UNDER_100, UriStatHistogramBucket.RANGE_100_300, 1);
        assertScheme(random, UriStatHistogramBucket.RANGE_100_300, UriStatHistogramBucket.RANGE_300_500, 2);
        assertScheme(random, UriStatHistogramBucket.RANGE_300_500, UriStatHistogramBucket.RANGE_500_1000, 3);
        assertScheme(random, UriStatHistogramBucket.RANGE_500_1000, UriStatHistogramBucket.RANGE_1000_3000, 4);
        assertScheme(random, UriStatHistogramBucket.RANGE_1000_3000, UriStatHistogramBucket.RANGE_3000_5000, 5);
        assertScheme(random, UriStatHistogramBucket.RANGE_3000_5000, UriStatHistogramBucket.RANGE_5000_8000, 6);
        assertScheme(random, UriStatHistogramBucket.RANGE_5000_8000, UriStatHistogramBucket.OVER_8000MS, 7);
    }

    void assertScheme(Random random, UriStatHistogramBucket prev, UriStatHistogramBucket current, int expectedIndex) {
        long value = Math.abs(random.nextLong());
        value %= (current.getTo() - current.getFrom());

        if (prev != null) {
            value += prev.getTo();
        }

        UriStatHistogramBucket result = UriStatHistogramBucket.getValue(value);

        Assert.assertEquals(current, result);
        Assert.assertEquals(expectedIndex, result.getIndex());
    }

}

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

package com.navercorp.pinpoint.profiler.context.storage.flush;

import com.navercorp.pinpoint.profiler.context.RandomTSpan;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class SpanEventThresholdConditionTest {

    private RandomTSpan randomSpanFactory = new RandomTSpan();

    @Test
    public void thresholdSizeTest() throws Exception {
        SpanEventThresholdCondition condition = new SpanEventThresholdCondition(20, 50);
        int threshold = condition.getThreshold();
        Assert.assertEquals(10, threshold);

        condition = new SpanEventThresholdCondition(20, 100);
        threshold = condition.getThreshold();
        Assert.assertEquals(20, threshold);

        condition = new SpanEventThresholdCondition(13, 30);
        threshold = condition.getThreshold();
        Assert.assertEquals(3, threshold);

        condition = new SpanEventThresholdCondition(20, 0);
        threshold = condition.getThreshold();
        Assert.assertEquals(0, threshold);

        condition = new SpanEventThresholdCondition(20, 1);
        threshold = condition.getThreshold();
        Assert.assertEquals(0, threshold);

        condition = new SpanEventThresholdCondition(1, 50);
        threshold = condition.getThreshold();
        Assert.assertEquals(0, threshold);

        condition = new SpanEventThresholdCondition(10);
        threshold = condition.getThreshold();
        Assert.assertEquals(10, threshold);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionTest1() throws Exception {
        SpanEventThresholdCondition condition = new SpanEventThresholdCondition(20, 101);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionTest2() throws Exception {
        SpanEventThresholdCondition condition = new SpanEventThresholdCondition(20, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionTest3() throws Exception {
        SpanEventThresholdCondition condition = new SpanEventThresholdCondition(0, 50);
    }

    @Test
    public void matchesTest() throws Exception {
        SpanEventThresholdCondition condition = new SpanEventThresholdCondition(20, 50);

        Span span = randomSpanFactory.createSpan(10);
        Assert.assertTrue(condition.matches(span, null));
        span = randomSpanFactory.createSpan(11);
        Assert.assertFalse(condition.matches(span, null));

        SpanChunk spanChunk = randomSpanFactory.createSpanChunk(10);
        Assert.assertTrue(condition.matches(spanChunk, null));
        spanChunk = randomSpanFactory.createSpanChunk(11);
        Assert.assertFalse(condition.matches(spanChunk, null));
    }

}

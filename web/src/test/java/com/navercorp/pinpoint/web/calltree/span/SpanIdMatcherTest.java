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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;

import org.junit.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanIdMatcherTest {
    @Test
    public void testApproximateMatch() throws Exception {
        List<SpanBo> matchSpanList = new ArrayList<SpanBo>();

        SpanBo spanBo1 = new SpanBo();
        spanBo1.setStartTime(1);
        matchSpanList.add(spanBo1);

        SpanBo spanBo2 = new SpanBo();
        spanBo2.setStartTime(2);
        matchSpanList.add(spanBo2);

        SpanIdMatcher spanIdMatcher = new SpanIdMatcher(matchSpanList);
        SpanBo match = spanIdMatcher.approximateMatch(1);
        Assert.assertTrue(match == spanBo1);

        Assert.assertEquals(1, spanIdMatcher.other().size());
        Assert.assertTrue(spanBo2 == spanIdMatcher.other().get(0));
    }

    @Test
    public void testApproximateMatchMinus() throws Exception {
        List<SpanBo> matchSpanList = new ArrayList<SpanBo>();

        SpanBo spanBo1 = new SpanBo();
        spanBo1.setStartTime(99);
        matchSpanList.add(spanBo1);

        SpanBo spanBo2 = new SpanBo();
        spanBo2.setStartTime(110);
        matchSpanList.add(spanBo2);

        SpanIdMatcher spanIdMatcher = new SpanIdMatcher(matchSpanList);
        SpanBo match = spanIdMatcher.approximateMatch(100);
        Assert.assertTrue(match == spanBo2);

        Assert.assertEquals(1, spanIdMatcher.other().size());
        Assert.assertTrue(spanBo1 == spanIdMatcher.other().get(0));
    }

    @Test
    public void testApproximateMatchEqualsCase() throws Exception {
        List<SpanBo> matchSpanList = new ArrayList<SpanBo>();

        SpanBo spanBo1 = new SpanBo();
        spanBo1.setStartTime(2);
        matchSpanList.add(spanBo1);

        SpanBo spanBo2 = new SpanBo();
        spanBo2.setStartTime(2);
        matchSpanList.add(spanBo2);

        SpanIdMatcher spanIdMatcher = new SpanIdMatcher(matchSpanList);
        SpanBo match = spanIdMatcher.approximateMatch(1);
        Assert.assertTrue(match == spanBo1);

        Assert.assertEquals(1, spanIdMatcher.other().size());
        Assert.assertTrue(spanBo2 == spanIdMatcher.other().get(0));
    }
}

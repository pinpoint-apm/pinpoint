package com.nhn.pinpoint.web.calltree.span;

import com.nhn.pinpoint.common.bo.SpanBo;
import junit.framework.Assert;
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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.vo.SpanHint;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Predicate;


public class SpanQueryBuilderTest {

    @Test
    public void spanFilter() {

        TransactionId txId = new TransactionId("agent", 1, 2);
        SpanHint spanHint = new SpanHint(100, 200, "appName");

        SpanQueryBuilder builder = new SpanQueryBuilder();
        Predicate<SpanBo> filter = builder.newSpanFilter(txId, spanHint);

        SpanBo span = new SpanBo();
        span.setTransactionId(txId);
        span.setCollectorAcceptTime(100);
        span.setElapsed(200);
        span.setApplicationId("appName");

        Assert.assertTrue(filter.test(span));
    }
}
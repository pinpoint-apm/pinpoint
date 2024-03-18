package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.calltree.span.SpanFilters;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.SpanHint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;


public class SpanQueryBuilderTest {

    private static final int COLLECTOR_ACCEPTOR_TIME = 100;
    private static final int RESPONSE_TIME = 200;
    private static final TransactionId txId = new TransactionId("agent", 1, 2);

    @Test
    public void spanQuery_build() {
        SpanHint spanHint = new SpanHint(COLLECTOR_ACCEPTOR_TIME, RESPONSE_TIME, "appName", "agentId");

        SpanQueryBuilder builder = new SpanQueryBuilder();
        GetTraceInfo traceInfo = new GetTraceInfo(txId, spanHint);
        SpanQuery spanQuery = builder.build(traceInfo);

        SpanBo span = new SpanBo();
        span.setTransactionId(new TransactionId("agent", 1, 2));
        span.setCollectorAcceptTime(100);
        span.setElapsed(200);
        span.setApplicationName("appName");
        span.setAgentId(AgentId.of("agentId"));

        Assertions.assertEquals(spanQuery.getTransactionId(), span.getTransactionId());
        Assertions.assertTrue(spanQuery.getSpanFilter().test(span));
    }

    @Test
    public void spanFilter() {

        SpanHint spanHint = new SpanHint(COLLECTOR_ACCEPTOR_TIME, RESPONSE_TIME, "appName");

        SpanQueryBuilder builder = new SpanQueryBuilder();
        Predicate<SpanBo> filter = builder.newSpanFilter(txId, spanHint);

        SpanBo span = new SpanBo();
        span.setTransactionId(txId);
        span.setCollectorAcceptTime(100);
        span.setElapsed(200);
        span.setApplicationName("appName");

        Assertions.assertTrue(filter.test(span));
    }

    @Test
    public void spanFilter_false() {

        SpanHint spanHint = new SpanHint(COLLECTOR_ACCEPTOR_TIME, RESPONSE_TIME, "appName");

        SpanQueryBuilder builder = new SpanQueryBuilder();
        Predicate<SpanBo> filter = builder.newSpanFilter(txId, spanHint);

        SpanBo span = new SpanBo();

        Assertions.assertFalse(filter.test(span));
    }

    @Test
    public void spanFilterWithAgentId_false() {

        SpanHint spanHint = new SpanHint(COLLECTOR_ACCEPTOR_TIME, RESPONSE_TIME, "appName", "agentId");

        SpanQueryBuilder builder = new SpanQueryBuilder();
        Predicate<SpanBo> filter = builder.newSpanFilter(txId, spanHint);

        SpanBo span = new SpanBo();

        Assertions.assertFalse(filter.test(span));
    }

    @Test
    public void spanFilter_txid() {

        Predicate<SpanBo> filter = SpanFilters.transactionIdFilter(txId);

        SpanBo span = new SpanBo();
        span.setTransactionId(txId);

        Assertions.assertTrue(filter.test(span));
    }

    @Test
    public void spanFilter_collectorAcceptTime() {

        Predicate<SpanBo> filter = SpanFilters.collectorAcceptTimeFilter(COLLECTOR_ACCEPTOR_TIME);

        SpanBo span = new SpanBo();
        span.setCollectorAcceptTime(COLLECTOR_ACCEPTOR_TIME);

        Assertions.assertTrue(filter.test(span));
    }

    @Test
    public void spanFilter_responseTime() {

        Predicate<SpanBo> filter = SpanFilters.responseTimeFilter(RESPONSE_TIME);

        SpanBo span = new SpanBo();
        span.setElapsed(RESPONSE_TIME);

        Assertions.assertTrue(filter.test(span));
    }

    @Test
    public void spanFilter_agentId() {

        Predicate<SpanBo> filter = SpanFilters.agentIdFilter("agentId");

        SpanBo span = new SpanBo();
        span.setAgentId(AgentId.of("agentId"));

        Assertions.assertTrue(filter.test(span));
    }
}
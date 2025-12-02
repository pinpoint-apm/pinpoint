/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.dao.hbase;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.trace.span.SpanFilters;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.SpanHint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;


public class SpanQueryBuilderTest {

    private static final int COLLECTOR_ACCEPTOR_TIME = 100;
    private static final int RESPONSE_TIME = 200;
    private static final TransactionId txId = TransactionId.of("agent", 1, 2);

    @Test
    public void spanQuery_build() {
        SpanHint spanHint = new SpanHint(COLLECTOR_ACCEPTOR_TIME, RESPONSE_TIME, "appName", "agentId");

        SpanQueryBuilder builder = new SpanQueryBuilder();
        GetTraceInfo traceInfo = new GetTraceInfo(txId, spanHint);
        SpanQuery spanQuery = builder.build(traceInfo);

        SpanBo span = new SpanBo();
        span.setTransactionId(TransactionId.of("agent", 1, 2));
        span.setCollectorAcceptTime(100);
        span.setElapsed(200);
        span.setApplicationName("appName");
        span.setAgentId("agentId");

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
        span.setAgentId("agentId");

        Assertions.assertTrue(filter.test(span));
    }
}
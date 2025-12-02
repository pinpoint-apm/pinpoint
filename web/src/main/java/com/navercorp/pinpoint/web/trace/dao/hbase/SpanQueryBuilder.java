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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.trace.span.SpanFilters;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.SpanHint;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.TimestampsFilter;

import java.util.Collections;
import java.util.function.Predicate;

public class SpanQueryBuilder {
    public SpanQueryBuilder() {
    }

    public SpanQuery build(GetTraceInfo getTraceInfo) {
        SpanHint hint = getTraceInfo.getHint();
        if (!hint.isSet()) {
            return new SpanQuery(getTraceInfo.getTransactionId());
        }

        Predicate<SpanBo> spanPredicate = newSpanFilter(getTraceInfo.getTransactionId(), hint);
        Filter hbaseFilter = getTimeStampFilter(getTraceInfo.getHint());
        return new SpanQuery(getTraceInfo.getTransactionId(), spanPredicate, hbaseFilter);
    }

    @VisibleForTesting
    Predicate<SpanBo> newSpanFilter(TransactionId transactionId, SpanHint spanHint) {
        SpanFilters.FilterBuilder builder = SpanFilters.newBuilder();

        builder.addFilter(SpanFilters.transactionIdFilter(transactionId));

        builder.addFilter(SpanFilters.collectorAcceptTimeFilter(spanHint.getCollectorAcceptorTime()));
        builder.addFilter(SpanFilters.responseTimeFilter(spanHint.getResponseTime()));

        if (spanHint.getApplicationName() != null) {
            builder.addFilter(SpanFilters.applicationIdFilter(spanHint.getApplicationName()));
        }
        if (spanHint.getAgentId() != null) {
            builder.addFilter(SpanFilters.agentIdFilter(spanHint.getAgentId()));
        }

        return builder.build();
    }


    private Filter getTimeStampFilter(SpanHint hint) {
        final long collectorAcceptorTime = hint.getCollectorAcceptorTime();
        if (collectorAcceptorTime >= 0) {
            return new TimestampsFilter(Collections.singletonList(collectorAcceptorTime));
        }
        return null;
    }

}

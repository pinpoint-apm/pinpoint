package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.calltree.span.SpanFilters;
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

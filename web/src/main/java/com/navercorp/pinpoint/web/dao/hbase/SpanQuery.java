package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.apache.hadoop.hbase.filter.Filter;

import java.util.Objects;
import java.util.function.Predicate;

public class SpanQuery {
    private final TransactionId transactionId;
    private final Predicate<SpanBo> spanFilter;
    private final Filter filter;

    public SpanQuery(TransactionId transactionId, Predicate<SpanBo> spanFilter, Filter filter) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.spanFilter = spanFilter;
        this.filter = filter;
    }

    public SpanQuery(TransactionId transactionId) {
        this(transactionId, null, null);
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public Predicate<SpanBo> getSpanFilter() {
        return spanFilter;
    }

    public Filter getHbaseFilter() {
        return filter;
    }

}

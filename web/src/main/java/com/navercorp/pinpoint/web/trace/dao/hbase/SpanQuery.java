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

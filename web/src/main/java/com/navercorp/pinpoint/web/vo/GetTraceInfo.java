/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class GetTraceInfo {

    private static final SpanHint NO_HINT = new SpanHint();

    private final TransactionId transactionId;
    private final SpanHint hint;

    public GetTraceInfo(TransactionId transactionId) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.hint = NO_HINT;
    }

    public GetTraceInfo(TransactionId transactionId, SpanHint hint) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.hint = Objects.requireNonNull(hint, "hint");
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public SpanHint getHint() {
        return hint;
    }

}

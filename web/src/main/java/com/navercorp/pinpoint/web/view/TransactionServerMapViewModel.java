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
package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.calltree.span.TraceState;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

import java.util.Objects;

public class TransactionServerMapViewModel {

    private final TransactionId transactionId;
    private final long spanId;
    private final Object mapView;

    public TransactionServerMapViewModel(TransactionId transactionId, long spanId,
                                         Object mapView) {
        this.transactionId = transactionId;
        this.spanId = spanId;
        this.mapView = Objects.requireNonNull(mapView, "mapView");
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId.toString();
    }

    @JsonProperty("spanId")
    public long getSpanId() {
        return spanId;
    }

    @JsonProperty("applicationMapData")
    public Object getApplicationMapData() {
        return mapView;
    }
}

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
import com.navercorp.pinpoint.web.applicationmap.MapView;

import java.util.Objects;

public class TransactionServerMapViewModel {

    private final String transactionId;
    private final long spanId;
    private final MapView mapView;

    public TransactionServerMapViewModel(String transactionId, long spanId,
                                         MapView mapView) {
        this.transactionId = transactionId;
        this.spanId = spanId;
        this.mapView = Objects.requireNonNull(mapView, "mapView");
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }

    @JsonProperty("spanId")
    public long getSpanId() {
        return spanId;
    }

    @JsonProperty("applicationMapData")
    public MapView getApplicationMapData() {
        return mapView;
    }
}

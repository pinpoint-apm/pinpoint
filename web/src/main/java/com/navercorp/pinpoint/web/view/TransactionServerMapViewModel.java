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
    private final RecordSet recordSet;
    private final TraceState.State completeState;

    private final LogLinkView logLinkView;
    private final Object mapView;

    public TransactionServerMapViewModel(TransactionId transactionId, long spanId,
                                         Object mapView,
                                         RecordSet recordSet, TraceState.State state,
                                         LogLinkView logLinkView) {
        this.transactionId = transactionId;
        this.spanId = spanId;

        this.mapView = Objects.requireNonNull(mapView, "mapView");

        this.recordSet = recordSet;
        this.completeState = state;
        this.logLinkView = Objects.requireNonNull(logLinkView, "logLinkView");
    }

    @JsonProperty("uri")
    public String getUri() {
        return recordSet.getUri();
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId.toString();
    }

    @JsonProperty("spanId")
    public long getSpanId() {
        return spanId;
    }

    @JsonProperty("agentId")
    public String getAgentId() {
        return recordSet.getAgentId();
    }

    @JsonProperty("agentName")
    public String getAgentName() {
        return recordSet.getAgentName();
    }

    @JsonProperty("applicationName")
    public String getApplicationName() {
        return recordSet.getApplicationName();
    }

    @JsonProperty("callStackStart")
    public long getCallStackStart() {
        return recordSet.getStartTime();
    }

    @JsonProperty("callStackEnd")
    public long getCallStackEnd() {
        return recordSet.getEndTime();
    }

    @JsonProperty("completeState")
    public String getCompleteState() {
        return completeState.toString();
    }

    @JsonProperty("loggingTransactionInfo")
    public boolean isLoggingTransactionInfo() {
        return recordSet.isLoggingTransactionInfo();
    }

    @JsonProperty("focusCallStackId")
    public int getFocusCallStackId() {
        return recordSet.getFocusCallStackId();
    }

    @JsonUnwrapped
    public LogLinkView getLogLink() {
        return logLinkView;
    }

    @JsonProperty("applicationMapData")
    public Object getApplicationMapData() {
        return mapView;
    }
}

/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

public class TransactionTimelineInfoViewModel {
    private final TransactionId transactionId;
    private final RecordSet recordSet;
    private final String traceViewerDataURL;

    public TransactionTimelineInfoViewModel(TransactionId transactionId, RecordSet recordSet, String traceViewerDataURL) {
        this.transactionId = transactionId;
        this.recordSet = recordSet;
        this.traceViewerDataURL = traceViewerDataURL;
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId.toString();
    }

    @JsonProperty("agentId")
    public String getAgentId() {
        return recordSet.getAgentId();
    }

    @Deprecated
    @JsonProperty("applicationId")
    public String getApplicationId() {
        return getApplicationName();
    }

    @JsonProperty("applicationName")
    public String getApplicationName() {
        return recordSet.getApplicationName();
    }

    @JsonProperty("traceViewerDataURL")
    public String getTraceViewerDataURL() {
        return traceViewerDataURL;
    }

}

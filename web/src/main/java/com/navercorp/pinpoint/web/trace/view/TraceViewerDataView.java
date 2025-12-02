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
package com.navercorp.pinpoint.web.trace.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.trace.model.TraceViewerData;

import java.util.List;
import java.util.Objects;

public class TraceViewerDataView {
    private final List<TraceViewerData.TraceEvent> traceEvents;

    public TraceViewerDataView(List<TraceViewerData.TraceEvent> traceEvents) {
        this.traceEvents = Objects.requireNonNull(traceEvents, "traceEvents");
    }

    @JsonProperty("traceEvents")
    public List<TraceViewerData.TraceEvent> getTraceEvents() {
        return traceEvents;
    }
}
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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;

import java.util.ArrayList;
import java.util.List;

public class OtlpTraceMapperData {

    List<SpanBo> spanBoList = new ArrayList<>();
    List<SpanChunkBo> spanChunkBoList = new ArrayList<>();
    List<AgentInfoBo> agentInfoBoList = new ArrayList<>();
    OtlpTraceCollectorRejectedSpan rejectedSpan = new OtlpTraceCollectorRejectedSpan();

    public List<SpanBo> getSpanBoList() {
        return spanBoList;
    }

    public void addSpanBo(SpanBo spanBo) {
        spanBoList.add(spanBo);
    }

    public List<SpanChunkBo> getSpanChunkBoList() {
        return spanChunkBoList;
    }

    public void addSpanChunkBo(SpanChunkBo spanChunkBo) {
        spanChunkBoList.add(spanChunkBo);
    }

    public List<AgentInfoBo> getAgentInfoBoList() {
        return agentInfoBoList;
    }

    public void addAgentInfoBo(AgentInfoBo agentInfoBo) {
        agentInfoBoList.add(agentInfoBo);
    }

    public OtlpTraceCollectorRejectedSpan getRejectedSpan() {
        return rejectedSpan;
    }
}

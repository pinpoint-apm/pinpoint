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

package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectorGrpcSpanFactory implements GrpcSpanFactory {

    private final SpanEventFilter spanEventFilter;
    private final AcceptedTimeService acceptedTimeService;
    private final GrpcSpanBinder grpcBinder;

    public CollectorGrpcSpanFactory(GrpcSpanBinder grpcBinder, SpanEventFilter spanEventFilter, AcceptedTimeService acceptedTimeService) {
        this.grpcBinder = Objects.requireNonNull(grpcBinder, "grpcBinder");
        this.spanEventFilter = spanEventFilter;
        this.acceptedTimeService = acceptedTimeService;
    }

    @Override
    public SpanBo buildSpanBo(PSpan pSpan, Header header) {
        final SpanBo spanBo = this.grpcBinder.bindSpanBo(pSpan, header);
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        spanBo.setCollectorAcceptTime(acceptedTime);

        final List<PSpanEvent> pSpanEventList = pSpan.getSpanEventList();
        List<SpanEventBo> spanEventBos = buildSpanEventBoList(pSpanEventList);
        spanBo.addSpanEventBoList(spanEventBos);

        return spanBo;
    }

    @Override
    public SpanChunkBo buildSpanChunkBo(PSpanChunk pSpanChunk, Header header) {
        final SpanChunkBo spanChunkBo = this.grpcBinder.bindSpanChunkBo(pSpanChunk, header);
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        spanChunkBo.setCollectorAcceptTime(acceptedTime);


        final List<PSpanEvent> pSpanEventList = pSpanChunk.getSpanEventList();
        List<SpanEventBo> spanEventList = buildSpanEventBoList(pSpanEventList);
        spanChunkBo.addSpanEventBoList(spanEventList);
        return spanChunkBo;
    }

    private List<SpanEventBo> buildSpanEventBoList(List<PSpanEvent> pSpanEventList) {
        final List<SpanEventBo> spanEventBos = this.grpcBinder.bindSpanEventBoList(pSpanEventList);
        if (applyFilter(spanEventBos)) {
            return filter(spanEventBos);
        }
        return spanEventBos;
    }

    private boolean applyFilter(List<SpanEventBo> spanEventBoList) {
        if (spanEventFilter == null) {
            return false;
        }
        for (SpanEventBo spanEventBo : spanEventBoList) {
            if (spanEventFilter.filter(spanEventBo) == SpanEventFilter.REJECT) {
                return true;
            }
        }
        return false;
    }

    private List<SpanEventBo> filter(List<SpanEventBo> spanEventBoList) {
        final List<SpanEventBo> filteredList = new ArrayList<>();
        for (SpanEventBo spanEventBo : spanEventBoList) {
            if (spanEventFilter.filter(spanEventBo) == SpanEventFilter.ACCEPT) {
                filteredList.add(spanEventBo);
            }
        }
        return filteredList;
    }


}

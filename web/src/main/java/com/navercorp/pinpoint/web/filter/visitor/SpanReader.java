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

package com.navercorp.pinpoint.web.filter.visitor;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanReader implements SpanAcceptor {

    private final List<SpanBo> spanBoList;

    public SpanReader(List<SpanBo> spanBoList) {
        this.spanBoList = Objects.requireNonNull(spanBoList, "spanBoList");
    }

    public boolean accept(SpanEventVisitor spanEventVisitor) {
        Objects.requireNonNull(spanEventVisitor, "spanEventVisitor");

        SpanVisitor spanEventAdaptor = new SpanEventVisitAdaptor(spanEventVisitor);
        return accept(spanEventAdaptor);
    }

    public boolean accept(SpanVisitor spanVisitor) {
        Objects.requireNonNull(spanVisitor, "spanVisitor");

        if (CollectionUtils.isEmpty(spanBoList)) {
            return SpanVisitor.REJECT;
        }

        for (SpanBo spanBo : spanBoList) {
            if (spanVisitor.visit(spanBo)) {
                return SpanVisitor.ACCEPT;
            }
            final List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
            if (visitSpanEventList(spanVisitor, spanEventBoList)) {
                return SpanVisitor.ACCEPT;
            }

            List<SpanChunkBo> spanChunkBoList = spanBo.getSpanChunkBoList();
            if (CollectionUtils.hasLength(spanChunkBoList)) {
                for (SpanChunkBo spanChunkBo : spanChunkBoList) {
                    if (spanVisitor.visit(spanChunkBo)) {
                        return SpanVisitor.ACCEPT;
                    }

                    List<SpanEventBo> spanChunkEventBoList = spanChunkBo.getSpanEventBoList();
                    if (visitSpanEventList(spanVisitor, spanChunkEventBoList)) {
                        return SpanVisitor.ACCEPT;
                    }
                }
            }
        }
        return SpanVisitor.REJECT;
    }

    private boolean visitSpanEventList(SpanVisitor spanVisitor, List<SpanEventBo> spanEventBoList) {
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return SpanVisitor.REJECT;
        }
        for (SpanEventBo spanEventBo : spanEventBoList) {
            if (spanVisitor.visit(spanEventBo)) {
                return SpanVisitor.ACCEPT;
            }
        }
        return SpanVisitor.REJECT;
    }

}

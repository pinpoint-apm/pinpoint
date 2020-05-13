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

package com.navercorp.pinpoint.web.filter.transaction;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.URLPatternFilter;
import com.navercorp.pinpoint.web.filter.visitor.SpanAcceptor;
import com.navercorp.pinpoint.web.filter.visitor.SpanEventVisitor;
import com.navercorp.pinpoint.web.filter.visitor.SpanReader;
import com.navercorp.pinpoint.web.filter.visitor.SpanVisitor;

import java.util.List;
import java.util.Objects;

public class WasToUnknownFilter implements Filter<LinkContext> {

    private final Filter<SpanEventBo> spanEventResponseConditionFilter;
    private final URLPatternFilter rpcUrlFilter;

    public WasToUnknownFilter(Filter<SpanEventBo> spanEventResponseConditionFilter, URLPatternFilter rpcUrlFilter) {
        this.spanEventResponseConditionFilter = Objects.requireNonNull(spanEventResponseConditionFilter, "spanEventResponseConditionFilter");
        this.rpcUrlFilter = Objects.requireNonNull(rpcUrlFilter, "rpcUrlFilter");
    }

    public boolean include(LinkContext spanContainer) {
        /*
         * WAS -> UNKNOWN
         */
        final List<SpanBo> fromNode = spanContainer.findFromNode();
        if (!rpcUrlFilter.accept(fromNode)) {
            return false;
        }
        SpanAcceptor acceptor = new SpanReader(fromNode);
        return acceptor.accept(new SpanEventVisitor() {
            @Override
            public boolean visit(SpanEventBo spanEventBo) {
                return filter(spanEventBo, spanContainer);
            }
        });
    }

    private boolean filter(SpanEventBo spanEventBo, LinkContext linkContext) {

        // check only whether a client exists or not.
        final ServiceType eventServiceType = linkContext.findServiceType(spanEventBo.getServiceType());
        if (eventServiceType.isRpcClient() && eventServiceType.isRecordStatistics()) {
            if (linkContext.isToApplicationName(spanEventBo.getDestinationId())) {
                if (spanEventResponseConditionFilter.include(spanEventBo)) {
                    return SpanVisitor.ACCEPT;
                }
            }
        }
        return SpanVisitor.REJECT;
    }
}

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
import com.navercorp.pinpoint.web.filter.visitor.SpanAcceptor;
import com.navercorp.pinpoint.web.filter.visitor.SpanEventVisitor;
import com.navercorp.pinpoint.web.filter.visitor.SpanReader;
import com.navercorp.pinpoint.web.filter.visitor.SpanVisitor;

import java.util.List;
import java.util.Objects;

/**
 * WAS -> BACKEND (non-WAS)
 */
public class WasToBackendFilter implements Filter<LinkContext> {
    private final Filter<SpanEventBo> spanEventResponseConditionFilter;

    public WasToBackendFilter(Filter<SpanEventBo> spanEventResponseConditionFilter) {
        this.spanEventResponseConditionFilter = Objects.requireNonNull(spanEventResponseConditionFilter, "spanEventResponseConditionFilter");
    }

    public boolean include(LinkContext linkContext) {
        final List<SpanBo> fromNode = linkContext.findFromNode();
        SpanAcceptor acceptor = new SpanReader(fromNode);
        return acceptor.accept(new SpanEventVisitor() {
            @Override
            public boolean visit(SpanEventBo spanEventBo) {
                return filter(spanEventBo, linkContext);
            }
        });
    }

    private boolean filter(SpanEventBo event, LinkContext linkContext) {
        final ServiceType eventServiceType = linkContext.findServiceType(event.getServiceType());
        if (linkContext.isToApplicationName(event.getDestinationId(), eventServiceType)) {
            if (spanEventResponseConditionFilter.include(event)) {
                return SpanVisitor.ACCEPT;
            }
        }
        return SpanVisitor.REJECT;
    }

}

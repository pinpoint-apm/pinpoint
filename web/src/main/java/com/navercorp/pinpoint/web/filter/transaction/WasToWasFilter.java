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
import com.navercorp.pinpoint.web.filter.RpcHint;
import com.navercorp.pinpoint.web.filter.RpcType;
import com.navercorp.pinpoint.web.filter.URLPatternFilter;
import com.navercorp.pinpoint.web.filter.agent.AgentFilterFactory;
import com.navercorp.pinpoint.web.filter.visitor.SpanAcceptor;
import com.navercorp.pinpoint.web.filter.visitor.SpanEventVisitor;
import com.navercorp.pinpoint.web.filter.visitor.SpanReader;
import com.navercorp.pinpoint.web.filter.visitor.SpanVisitor;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class WasToWasFilter implements Filter<LinkContext> {
    private final Filter<SpanBo> spanResponseConditionFilter;

    private final Filter<SpanEventBo> spanEventResponseConditionFilter;

    private final AgentFilterFactory agentFilterFactory;


    private final URLPatternFilter acceptURLFilter;

    private final URLPatternFilter rpcUrlFilter;
    private final List<RpcHint> rpcHintList;

    public WasToWasFilter(Filter<SpanBo> spanResponseConditionFilter,
                          Filter<SpanEventBo> spanEventResponseConditionFilter,
                          URLPatternFilter acceptURLFilter,
                          AgentFilterFactory agentFilterFactory,
                          URLPatternFilter rpcUrlFilter,
                          List<RpcHint> rpcHintList) {
        this.spanResponseConditionFilter = Objects.requireNonNull(spanResponseConditionFilter, "spanResponseConditionFilter");
        this.spanEventResponseConditionFilter = Objects.requireNonNull(spanEventResponseConditionFilter, "spanEventResponseConditionFilter");
        this.acceptURLFilter = Objects.requireNonNull(acceptURLFilter, "acceptURLFilter");

        this.agentFilterFactory = Objects.requireNonNull(agentFilterFactory, "agentFilterFactory");

        this.rpcUrlFilter = Objects.requireNonNull(rpcUrlFilter, "rpcUrlFilter");
        this.rpcHintList = Objects.requireNonNull(rpcHintList, "rpcHintList");
    }

    public boolean include(LinkContext transaction) {
        /*
         * WAS -> WAS
         * if destination is a "WAS", the span of src and dest may exist. need to check if be circular or not.
         * find src first. span (from, to) may exist more than one. so (spanId == parentSpanID) should be checked.
         */

        final List<SpanBo> fromSpanList = transaction.findFromNode();
        if (fromSpanList.isEmpty()) {
            // from span not found
            return Filter.REJECT;
        }
        final List<SpanBo> toSpanList = transaction.findToNode(acceptURLFilter);
        if (!toSpanList.isEmpty()) {

            // from -> to compare SpanId & pSpanId filter
            final boolean exactMatch = wasToWasExactMatch(fromSpanList, toSpanList);
            if (exactMatch) {
                return Filter.ACCEPT;
            }
        }
        if (this.agentFilterFactory.toAgentExist()) {
            // fast skip. toAgent filtering condition exist.
            // url filter not available.
            return Filter.REJECT;
        }

        // Check for url pattern should now be done on the caller side (from spans) as to spans are missing at this point
        if (!rpcUrlFilter.accept(fromSpanList)) {
            return Filter.REJECT;
        }

        // if agent filter is FromAgentFilter or AcceptAgentFilter(agent filter is not selected), url filtering is available.
        return fromBaseFilter(fromSpanList, transaction);
    }

    private boolean fromBaseFilter(List<SpanBo> fromSpanList, LinkContext transaction) {
        // from base filter. hint base filter
        // exceptional case
        // 1. remote call fail
        // 2. span packet lost.
        if (rpcHintList.isEmpty()) {
            // fast skip. There is nothing more we can do if rpcHintList is empty.
            return false;
        }
        SpanAcceptor acceptor = new SpanReader(fromSpanList);
        return acceptor.accept(new SpanEventVisitor() {
            @Override
            public boolean visit(SpanEventBo spanEventBo) {
                return filterByRpcHints(spanEventBo, transaction);
            }
        });
    }

    private boolean filterByRpcHints(SpanEventBo spanEventBo, LinkContext transaction) {
        if (filterByRpcHints(rpcHintList, spanEventBo, transaction)) {
            return SpanVisitor.ACCEPT;
        }
        return SpanVisitor.REJECT;
    }

    private boolean filterByRpcHints(List<RpcHint> rpcHintList, SpanEventBo event, LinkContext transaction) {
        final ServiceType eventServiceType = transaction.findServiceType(event.getServiceType());
        if (!eventServiceType.isRecordStatistics()) {
            return false;
        }
        if (eventServiceType.isRpcClient() || eventServiceType.isQueue()) {
            // check rpc call fail
            // There are also cases where multiple applications receiving the same request from the caller node
            // but not all of them have agents installed. RpcHint is used for such cases as acceptUrlFilter will
            // reject these transactions.
            for (RpcHint rpcHint : rpcHintList) {
                for (RpcType rpcType : rpcHint.getRpcTypeList()) {
                    if (rpcType.isMatched(event.getDestinationId(), eventServiceType.getCode())) {
                        if (spanEventResponseConditionFilter.include(event)) {
                            return Filter.ACCEPT;
                        }
                    }
                }
            }
        }
        return Filter.REJECT;
    }

    private boolean wasToWasExactMatch(List<SpanBo> fromSpanList, List<SpanBo> toSpanList) {
        // from -> to compare SpanId & pSpanId filter
        for (SpanBo fromSpanBo : fromSpanList) {
            for (SpanBo toSpanBo : toSpanList) {
                if (fromSpanBo == toSpanBo) {
                    // skip same object;
                    continue;
                }
                if (fromSpanBo.getSpanId() == toSpanBo.getParentSpanId()) {
                    if (spanResponseConditionFilter.include(toSpanBo)) {
                        return Filter.ACCEPT;
                    }
                }
            }
        }
        return Filter.REJECT;
    }
}

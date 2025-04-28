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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LinkMap {
    private final MultiValueMap<LongPair, Node> spanToLinkMap;

    public LinkMap(MultiValueMap<LongPair, Node> spanToLinkMap, List<Node> duplicatedNodeList) {
        this.spanToLinkMap = Objects.requireNonNull(spanToLinkMap, "spanToLinkMap");
    }

    public static LinkMap buildLinkMap(NodeList nodeList, TraceState traceState, Predicate<SpanBo> focusFilter, ServiceTypeRegistryService serviceTypeRegistryService) {
        Objects.requireNonNull(focusFilter, "focusFilter");

        final MultiValueMap<LongPair, Node> spanToLinkMap = new LinkedMultiValueMap<>();

        // for performance & remove duplicate span
        final List<Node> duplicatedNodeList = new ArrayList<>();
        for (Node node : nodeList) {
            final SpanBo span = node.getSpanBo();
            final LongPair spanIdPairKey = new LongPair(span.getParentSpanId(), span.getSpanId());
            // check duplicated span
            Node firstNode = spanToLinkMap.getFirst(spanIdPairKey);
            if (firstNode == null) {
                spanToLinkMap.add(spanIdPairKey, node);
            } else {
                ServiceType serviceType = serviceTypeRegistryService.findServiceType(span.getServiceType());
                if (serviceType.isQueue() && firstNode.getSpanBo().getServiceType() == serviceType.getCode()) {
                    spanToLinkMap.add(spanIdPairKey, node);
                } else {
                    traceState.progress();
                    spanToLinkMap.add(spanIdPairKey, node);
                }
            }
        }

        return new LinkMap(spanToLinkMap, duplicatedNodeList);
    }

    public List<Node> findNode(Link link) {
        Objects.requireNonNull(link, "link");

        final LongPair key = new LongPair(link.getSpanId(), link.getNextSpanId());
        return this.spanToLinkMap.get(key);
    }
}

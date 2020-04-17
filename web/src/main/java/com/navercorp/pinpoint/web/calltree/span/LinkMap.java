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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;

import com.navercorp.pinpoint.common.server.util.LongPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LinkMap {

    private static final Logger logger = LoggerFactory.getLogger(LinkMap.class);

    private final MultiValueMap<LongPair, Node> spanToLinkMap;

    private final List<Node> duplicatedNodeList;

    public LinkMap(MultiValueMap<LongPair, Node> spanToLinkMap, List<Node> duplicatedNodeList) {
        this.spanToLinkMap = spanToLinkMap;
        this.duplicatedNodeList = duplicatedNodeList;
    }

    public static LinkMap buildLinkMap(List<Node> nodeList, TraceState traceState, long collectorAcceptTime, ServiceTypeRegistryService serviceTypeRegistryService) {
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
                    // duplicated span, choose focus span
                    if (span.getCollectorAcceptTime() == collectorAcceptTime) {
                        // replace value
                        spanToLinkMap.put(spanIdPairKey, Collections.singletonList(node));
                        duplicatedNodeList.add(node);
                        logger.warn("Duplicated span - choose focus {}", node);
                    } else {
                        // add remove list
                        duplicatedNodeList.add(node);
                        logger.warn("Duplicated span - ignored second {}", node);
                    }
                }
            }
        }

        // clean duplicated node
        nodeList.removeAll(duplicatedNodeList);
        return new LinkMap(spanToLinkMap, duplicatedNodeList);
    }

    public List<Node> findNode(Link link) {
        Objects.requireNonNull(link, "link");

        final LongPair key = new LongPair(link.getSpanId(), link.getNextSpanId());
        return this.spanToLinkMap.get(key);
    }


    public List<Node> getDuplicatedNodeList() {
        return duplicatedNodeList;
    }

}

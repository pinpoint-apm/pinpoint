/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.filter.agent.*;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilterFactory;
import com.navercorp.pinpoint.web.filter.visitor.SpanAcceptor;
import com.navercorp.pinpoint.web.filter.visitor.SpanReader;
import com.navercorp.pinpoint.web.filter.visitor.SpanVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author netspider
 * @author emeroad
 */
public class LinkFilter implements Filter<SpanBo> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<ServiceType> fromServiceDescList;
    private final String fromApplicationName;

    private final List<ServiceType> toServiceDescList;
    private final String toApplicationName;

    private final ResponseTimeFilter responseTimeFilter;

    private final ExecutionType executionType;

    private final FilterHint filterHint;

    private final AgentFilterFactory agentFilterFactory;
    private final AgentFilter fromAgentFilter;
    private final AgentFilter toAgentFilter;

    private final Filter<SpanBo> filter;

    private final List<RpcHint> rpcHintList;

    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    private final URLPatternFilter acceptURLFilter;
    private final URLPatternFilter rpcUrlFilter;

    public LinkFilter(FilterDescriptor filterDescriptor, FilterHint filterHint, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {
        Objects.requireNonNull(filterDescriptor, "filterDescriptor");
        Objects.requireNonNull(filterHint, "filterHint");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.annotationKeyRegistryService = Objects.requireNonNull(annotationKeyRegistryService, "annotationKeyRegistryService");

        final String fromServiceType = filterDescriptor.getFromServiceType();
        this.fromServiceDescList = serviceTypeRegistryService.findDesc(fromServiceType);
        if (this.fromServiceDescList == null) {
            throw new IllegalArgumentException("fromServiceDescList not found. fromServiceType:" + fromServiceType);
        }

        this.fromApplicationName = filterDescriptor.getFromApplicationName();
        Objects.requireNonNull(this.fromApplicationName, "fromApplicationName");

        final String toServiceType = filterDescriptor.getToServiceType();
        this.toServiceDescList = serviceTypeRegistryService.findDesc(toServiceType);
        if (toServiceDescList == null) {
            throw new IllegalArgumentException("toServiceDescList not found. toServiceDescList:" + toServiceType);
        }

        this.toApplicationName = filterDescriptor.getToApplicationName();
        Objects.requireNonNull(this.toApplicationName, "toApplicationName");

        this.responseTimeFilter = createResponseTimeFilter(filterDescriptor);

        this.executionType = getExecutionType(filterDescriptor);

        this.filterHint = filterHint;
        Objects.requireNonNull(this.filterHint, "filterHint");

        final String fromAgentName = filterDescriptor.getFromAgentName();
        final String toAgentName = filterDescriptor.getToAgentName();

        this.agentFilterFactory = new AgentFilterFactory(fromAgentName, toAgentName);
        logger.debug("agentFilterFactory:{}", agentFilterFactory);
        this.fromAgentFilter = agentFilterFactory.createFromAgentFilter();
        this.toAgentFilter = agentFilterFactory.createToAgentFilter();

        this.filter = resolveFilter();
        logger.info("filter:{}", filter);

        this.rpcHintList = this.filterHint.getRpcHintList(toApplicationName);

        // TODO fix : fromSpan base rpccall filter
        this.acceptURLFilter = createAcceptUrlFilter(filterDescriptor);
        this.rpcUrlFilter = createRpcUrlFilter(filterDescriptor);
        logger.info("acceptURLFilter:{}", acceptURLFilter);
    }

    private URLPatternFilter createAcceptUrlFilter(FilterDescriptor filterDescriptor) {
        final String urlPattern = filterDescriptor.getUrlPattern();
        if (StringUtils.isEmpty(urlPattern)) {
            return new BypassURLPatternFilter();
        }
        // TODO remove decode
        return new AcceptUrlFilter(urlPattern);
    }

    private URLPatternFilter createRpcUrlFilter(FilterDescriptor filterDescriptor) {
        final String urlPattern = filterDescriptor.getUrlPattern();
        if (StringUtils.isEmpty(urlPattern)) {
            return new BypassURLPatternFilter();
        }
        return new RpcURLPatternFilter(urlPattern, serviceTypeRegistryService, annotationKeyRegistryService);
    }

    private ResponseTimeFilter createResponseTimeFilter(FilterDescriptor filterDescriptor) {
        final ResponseTimeFilterFactory factory = new ResponseTimeFilterFactory(filterDescriptor.getFromResponseTime(), filterDescriptor.getResponseTo());
        return factory.createFilter();
    }


    private ExecutionType getExecutionType(FilterDescriptor filterDescriptor) {
        final Boolean includeException = filterDescriptor.getIncludeException();
        if (includeException == null) {
            return ExecutionType.ALL;
        }
        if (includeException) {
            return ExecutionType.FAIL_ONLY;
        }
        return ExecutionType.SUCCESS_ONLY;
    }


    enum ExecutionType {
        ALL,
        SUCCESS_ONLY,
        FAIL_ONLY
    }

    private Filter<SpanBo> resolveFilter() {
        if (includeWas(fromServiceDescList) && includeWas(toServiceDescList)) {
            return new WasToWasFilter();
        }
        if (includeUser(fromServiceDescList) && includeWas(toServiceDescList)) {
            return new UserToWasFilter();
        }
        if (includeWas(fromServiceDescList) && includeUnknown(toServiceDescList)) {
            return new WasToUnknownFilter();
        }
        if (includeWas(fromServiceDescList) && includeQueue(toServiceDescList)) {
            return new WasToQueueFilter();
        }
        if (includeQueue(fromServiceDescList) && includeWas(toServiceDescList)) {
            return new QueueToWasFilter();
        }
        // TODO toServiceDescList check logic not exist.
//        if (includeWas(fromServiceDescList) && isBackEnd????()) {
        if (includeWas(fromServiceDescList)) {
            return new WasToBackendFilter();
        }
        logger.warn("unsupported filter type:{}", this);
        throw new IllegalArgumentException("filter resolve fail:" + this);
    }

    private boolean checkResponseCondition(long elapsed, boolean hasError) {
        if (responseTimeFilter.accept(elapsed) == ResponseTimeFilter.REJECT) {
            return false;
        }

        switch (executionType) {
            case ALL: {
                return SpanVisitor.ACCEPT;
            }
            case FAIL_ONLY: {
                // is error
                if (hasError == true) {
                    return SpanVisitor.ACCEPT;
                }
                return SpanVisitor.REJECT;
            }
            case SUCCESS_ONLY: {
                // is success
                if (hasError == false) {
                    return SpanVisitor.ACCEPT;
                }
                return SpanVisitor.REJECT;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported ExecutionType:" + executionType);
            }
        }
    }

    @Override
    public boolean include(List<SpanBo> transaction) {
        return filter.include(transaction);
    }

    /**
     * USER -> WAS
     */
    public class UserToWasFilter implements Filter<SpanBo> {
        public boolean include(List<SpanBo> transaction) {
            final List<SpanBo> toNode = findToNode(transaction);
            for (SpanBo span : toNode) {
                if (span.isRoot()) {
                    if (checkResponseCondition(span.getElapsed(), isError(span))) {
                        return SpanVisitor.ACCEPT;
                    }
                }
            }
            return SpanVisitor.REJECT;
        }
    }

    private boolean isError(SpanBo span) {
        return span.getErrCode() > 0;
    }

    class WasToUnknownFilter implements Filter<SpanBo> {
        public boolean include(List<SpanBo> transaction) {
            /*
             * WAS -> UNKNOWN
             */
            final List<SpanBo> fromNode = findFromNode(transaction);
            if (!rpcUrlFilter.accept(fromNode)) {
                return false;
            }
            SpanAcceptor acceptor = new SpanReader(fromNode);
            return acceptor.accept(this::filter);
        }

        private boolean filter(SpanEventBo spanEventBo) {

            // check only whether a client exists or not.
            final ServiceType eventServiceType = serviceTypeRegistryService.findServiceType(spanEventBo.getServiceType());
            if (eventServiceType.isRpcClient() && eventServiceType.isRecordStatistics()) {
                if (toApplicationName.equals(spanEventBo.getDestinationId())) {
                    if (checkResponseCondition(spanEventBo.getEndElapsed(), spanEventBo.hasException())) {
                        return SpanVisitor.ACCEPT;
                    }
                }
            }
            return SpanVisitor.REJECT;
        }
    }

    /**
     * WAS -> BACKEND (non-WAS)
     */
    class WasToBackendFilter implements Filter<SpanBo> {
        public boolean include(List<SpanBo> transaction) {
            final List<SpanBo> fromNode = findFromNode(transaction);
            SpanAcceptor acceptor = new SpanReader(fromNode);
            return acceptor.accept(this::filter);
        }

        private boolean filter(SpanEventBo event) {
            final ServiceType eventServiceType = serviceTypeRegistryService.findServiceType(event.getServiceType());
            if (isToNode(event.getDestinationId(), eventServiceType)) {
                if (checkResponseCondition(event.getEndElapsed(), event.hasException())) {
                    return SpanVisitor.ACCEPT;
                }
            }
            return SpanVisitor.REJECT;
        }
    }

    /**
     * WAS -> WAS
     */
    class WasToWasFilter implements Filter<SpanBo> {
        public boolean include(List<SpanBo> transaction) {
            /*
             * WAS -> WAS
             * if destination is a "WAS", the span of src and dest may exist. need to check if be circular or not.
             * find src first. span (from, to) may exist more than one. so (spanId == parentSpanID) should be checked.
             */

            final List<SpanBo> fromSpanList = findFromNode(transaction);
            if (fromSpanList.isEmpty()) {
                // from span not found
                return Filter.REJECT;
            }
            final List<SpanBo> toSpanList = findToNode(transaction);
            if (!toSpanList.isEmpty()) {

                // from -> to compare SpanId & pSpanId filter
                final boolean exactMatch = wasToWasExactMatch(fromSpanList, toSpanList);
                if (exactMatch) {
                    return Filter.ACCEPT;
                }
            }
            if (isToAgentFilter()) {
                // fast skip. toAgent filtering condition exist.
                // url filter not available.
                return Filter.REJECT;
            }

            // Check for url pattern should now be done on the caller side (from spans) as to spans are missing at this point
            if (!rpcUrlFilter.accept(fromSpanList)) {
                return Filter.REJECT;
            }

            // if agent filter is FromAgentFilter or AcceptAgentFilter(agent filter is not selected), url filtering is available.
            return fromBaseFilter(fromSpanList);
        }
    }

    /**
     * WAS -> Queue (virtual)
     * Should be the same as {@link WasToBackendFilter}
     */
    class WasToQueueFilter extends WasToBackendFilter {

    }

    /**
     * Queue (virtual) -> WAS
     */
    class QueueToWasFilter implements Filter<SpanBo> {

        public boolean include(List<SpanBo> transaction) {
            final List<SpanBo> toNode = findToNode(transaction);
            if (logger.isDebugEnabled()) {
                logger.debug("matching toNode spans: {}", toNode);
            }
            for (SpanBo span : toNode) {
                if (fromApplicationName.equals(span.getAcceptorHost())) {
                    if (checkResponseCondition(span.getElapsed(), isError(span))) {
                        return Filter.ACCEPT;
                    }
                }
            }
            return Filter.REJECT;
        }
    }

    private boolean fromBaseFilter(List<SpanBo> fromSpanList) {
        // from base filter. hint base filter
        // exceptional case
        // 1. remote call fail
        // 2. span packet lost.
        if (rpcHintList.isEmpty()) {
            // fast skip. There is nothing more we can do if rpcHintList is empty.
            return false;
        }
        SpanAcceptor acceptor = new SpanReader(fromSpanList);
        return acceptor.accept(this::filterByRpcHints);
    }

    private boolean filterByRpcHints(SpanEventBo spanEventBo) {
        if (filterByRpcHints(rpcHintList, spanEventBo)) {
            return SpanVisitor.ACCEPT;
        }
        return SpanVisitor.REJECT;
    }

    private boolean filterByRpcHints(List<RpcHint> rpcHintList, SpanEventBo event) {
        final ServiceType eventServiceType = serviceTypeRegistryService.findServiceType(event.getServiceType());
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
                        if (checkResponseCondition(event.getEndElapsed(), event.hasException())) {
                            return Filter.ACCEPT;
                        }
                    }
                }
            }
        }
        return Filter.REJECT;
    }

    private boolean isToAgentFilter() {
        return this.agentFilterFactory.toAgentExist();
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
                    final int elapsed = toSpanBo.getElapsed();
                    final boolean error = isError(toSpanBo);
                    if (checkResponseCondition(elapsed, error)) {
                        return Filter.ACCEPT;
                    }
                }
            }
        }
        return Filter.REJECT;
    }

    private List<SpanBo> findFromNode(List<SpanBo> transaction) {
        final List<SpanBo> node = findNode(transaction, fromApplicationName, fromServiceDescList, fromAgentFilter);
//        RpcURLPatternFilter rpcURLPatternFilter = new RpcURLPatternFilter("/**/*");
//        if (!rpcURLPatternFilter.accept(node)) {
//            return Collections.emptyList();
//        }
        return node;
    }

    private List<SpanBo> findToNode(List<SpanBo> transaction) {
        final List<SpanBo> node = findNode(transaction, toApplicationName, toServiceDescList, toAgentFilter);
        if (!acceptURLFilter.accept(node)) {
            return Collections.emptyList();
        }
        return node;
    }


    private List<SpanBo> findNode(List<SpanBo> nodeList, String findApplicationName, List<ServiceType> findServiceCode, AgentFilter agentFilter) {
        List<SpanBo> findList = null;
        for (SpanBo span : nodeList) {
            final ServiceType applicationServiceType = serviceTypeRegistryService.findServiceType(span.getApplicationServiceType());
            if (findApplicationName.equals(span.getApplicationId()) && includeServiceType(findServiceCode, applicationServiceType)) {
                // apply preAgentFilter
                if (agentFilter.accept(span.getAgentId())) {
                    if (findList == null) {
                        findList = new ArrayList<>();
                    }
                    findList.add(span);
                }
            }
        }
        if (findList == null) {
            return Collections.emptyList();
        }
        return findList;
    }


    private boolean isToNode(String applicationId, ServiceType serviceType) {
        return this.toApplicationName.equals(applicationId) && includeServiceType(this.toServiceDescList, serviceType);
    }

    private boolean includeUser(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isUser()) {
                return Filter.ACCEPT;
            }
        }
        return Filter.REJECT;
    }

    private boolean includeUnknown(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isUnknown()) {
                return Filter.ACCEPT;
            }
        }
        return Filter.REJECT;
    }

    private boolean includeWas(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isWas()) {
                return Filter.ACCEPT;
            }
        }
        return Filter.REJECT;
    }

    private boolean includeQueue(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isQueue()) {
                return Filter.ACCEPT;
            }
        }
        return Filter.REJECT;
    }

    private boolean includeServiceType(List<ServiceType> serviceTypeList, ServiceType targetServiceType) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType == targetServiceType) {
                return Filter.ACCEPT;
            }
        }
        return Filter.REJECT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LinkFilter{");
        sb.append("fromServiceDescList=").append(fromServiceDescList);
        sb.append(", fromApplicationName='").append(fromApplicationName).append('\'');
        sb.append(", toServiceDescList=").append(toServiceDescList);
        sb.append(", toApplicationName='").append(toApplicationName).append('\'');
        sb.append(", responseTimeFilter=").append(responseTimeFilter);
        sb.append(", executionType=").append(executionType);
        sb.append(", filterHint=").append(filterHint);
        sb.append(", agentFilterFactory=").append(agentFilterFactory);
        sb.append(", fromAgentFilter=").append(fromAgentFilter);
        sb.append(", toAgentFilter=").append(toAgentFilter);
        sb.append(", filter=").append(filter);
        sb.append(", rpcHintList=").append(rpcHintList);
        sb.append(", acceptURLFilter=").append(acceptURLFilter);
        sb.append('}');
        return sb.toString();
    }
}

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


import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.filter.agent.*;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 *
 * @author netspider
 * @author emeroad
 *
 */
public class LinkFilter implements Filter {
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

    private final FilterType filterType;

    private final List<RpcHint> rpcHintList;

    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    private final URLPatternFilter acceptURLFilter;
    private final URLPatternFilter rpcUrlFilter;

    public LinkFilter(FilterDescriptor filterDescriptor, FilterHint filterHint, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {
        if (filterDescriptor == null) {
            throw new NullPointerException("filter descriptor must not be null");
        }
        if (filterHint == null) {
            throw new NullPointerException("filterHint must not be null");
        }
        if (serviceTypeRegistryService == null) {
            throw new NullPointerException("serviceTypeRegistryService must not be null");
        }
        if (annotationKeyRegistryService == null) {
            throw new NullPointerException("annotationKeyRegistryService must not be null");
        }

        this.serviceTypeRegistryService = serviceTypeRegistryService;
        this.annotationKeyRegistryService = annotationKeyRegistryService;

        final String fromServiceType = filterDescriptor.getFromServiceType();
        this.fromServiceDescList = serviceTypeRegistryService.findDesc(fromServiceType);
        if (this.fromServiceDescList == null) {
            throw new IllegalArgumentException("fromServiceDescList not found. fromServiceType:" + fromServiceType);
        }

        this.fromApplicationName = filterDescriptor.getFromApplicationName();
        Assert.notNull(this.fromApplicationName, "fromApplicationName must not be null");

        final String toServiceType = filterDescriptor.getToServiceType();
        this.toServiceDescList = serviceTypeRegistryService.findDesc(toServiceType);
        if (toServiceDescList == null) {
            throw new IllegalArgumentException("toServiceDescList not found. toServiceDescList:" + toServiceType);
        }

        this.toApplicationName = filterDescriptor.getToApplicationName();
        Assert.notNull(this.toApplicationName, "toApplicationName must not be null");

        this.responseTimeFilter = createResponseTimeFilter(filterDescriptor);

        this.executionType = getExecutionType(filterDescriptor);

        this.filterHint = filterHint;
        Assert.notNull(this.filterHint, "filterHint must not be null");

        final String fromAgentName = filterDescriptor.getFromAgentName();
        final String toAgentName = filterDescriptor.getToAgentName();

        this.agentFilterFactory = new AgentFilterFactory(fromAgentName, toAgentName);
        logger.debug("agentFilterFactory:{}", agentFilterFactory);
        this.fromAgentFilter = agentFilterFactory.createFromAgentFilter();
        this.toAgentFilter = agentFilterFactory.createToAgentFilter();

        this.filterType = getFilterType();
        logger.info("filterType:{}", filterType);

        this.rpcHintList = this.filterHint.getRpcHintList(toApplicationName);

        // TODO fix : fromSpan base rpccall filter
        this.acceptURLFilter = createAcceptUrlFilter(filterDescriptor);
        this.rpcUrlFilter = createRpcUrlFilter(filterDescriptor);
        logger.info("acceptURLFilter:{}", acceptURLFilter);
    }

    private URLPatternFilter createAcceptUrlFilter(FilterDescriptor filterDescriptor) {
        if (StringUtils.isEmpty(filterDescriptor.getUrlPattern())) {
            return new BypassURLPatternFilter();
        }
        // TODO remove decode
        return new AcceptUrlFilter(filterDescriptor.getUrlPattern());
    }

    private URLPatternFilter createRpcUrlFilter(FilterDescriptor filterDescriptor) {
        if (StringUtils.isEmpty(filterDescriptor.getUrlPattern())) {
            return new BypassURLPatternFilter();
        }
        return new RpcURLPatternFilter(filterDescriptor.getUrlPattern(), serviceTypeRegistryService, annotationKeyRegistryService);
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

    enum FilterType {
        WAS_TO_WAS,
        USER_TO_WAS,
        WAS_TO_UNKNOWN,
        WAS_TO_BACKEND,
        WAS_TO_QUEUE,
        QUEUE_TO_WAS,
        UNSUPPORTED
    }

    enum ExecutionType {
        ALL,
        SUCCESS_ONLY,
        FAIL_ONLY
    }

    private FilterType getFilterType() {
        if (includeWas(fromServiceDescList) && includeWas(toServiceDescList)) {
            return FilterType.WAS_TO_WAS;
        }
        if (includeUser(fromServiceDescList) && includeWas(toServiceDescList)) {
            return FilterType.USER_TO_WAS;
        }
        if (includeWas(fromServiceDescList) && includeUnknown(toServiceDescList)) {
            return FilterType.WAS_TO_UNKNOWN;
        }
        if (includeWas(fromServiceDescList) && includeQueue(toServiceDescList)) {
            return FilterType.WAS_TO_QUEUE;
        }
        if (includeQueue(fromServiceDescList) && includeWas(toServiceDescList)) {
            return FilterType.QUEUE_TO_WAS;
        }
        // TODO toServiceDescList check logic not exist.
//        if (includeWas(fromServiceDescList) && isBackEnd????()) {
        if (includeWas(fromServiceDescList)) {
            return FilterType.WAS_TO_BACKEND;
        }
        return FilterType.UNSUPPORTED;
    }

    private boolean checkResponseCondition(long elapsed, boolean hasError) {
        if (responseTimeFilter.accept(elapsed) == ResponseTimeFilter.REJECT) {
            return false;
        }

        switch (executionType) {
            case ALL: {
                return true;
            }
            case FAIL_ONLY: {
                // is error
                if (hasError == true) {
                    return true;
                }
                return false;
            }
            case SUCCESS_ONLY: {
                // is success
                if (hasError == false) {
                    return true;
                }
                return false;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported ExecutionType:" + executionType);
            }
        }
    }

    @Override
    public boolean include(List<SpanBo> transaction) {
        switch (this.filterType) {
            case USER_TO_WAS: {
                return userToWasFilter(transaction);
            }
            case WAS_TO_UNKNOWN: {
                return wasToUnknownFilter(transaction);
            }
            case WAS_TO_WAS: {
                return wasToWasFilter(transaction);
            }
            case WAS_TO_QUEUE: {
                return wasToQueueFilter(transaction);
            }
            case QUEUE_TO_WAS: {
                return queueToWasFilter(transaction);
            }
            case WAS_TO_BACKEND: {
                return wasToBackendFilter(transaction);
            }
            default: {
                logger.warn("unsupported filter type:{}", this);
                throw new IllegalArgumentException("unsupported filter type:");
            }
        }
    }

    /**
     * USER -> WAS
     */
    private boolean userToWasFilter(List<SpanBo> transaction) {
        final List<SpanBo> toNode = findToNode(transaction);
        for (SpanBo span : toNode) {
            if (span.isRoot()) {
                if (checkResponseCondition(span.getElapsed(), isError(span))) {
                    return true;
                }

            }
        }
        return false;
    }

    private boolean isError(SpanBo span) {
        return span.getErrCode() > 0;
    }

    private boolean wasToUnknownFilter(List<SpanBo> transaction) {
        /*
         * WAS -> UNKNOWN
         */
        final List<SpanBo> fromNode = findFromNode(transaction);
        if (!rpcUrlFilter.accept(fromNode)) {
            return false;
        }
        for (SpanBo span : fromNode) {
            final List<SpanEventBo> eventBoList = span.getSpanEventBoList();
            if (eventBoList == null) {
                continue;
            }

            for (SpanEventBo event : eventBoList) {
                // check only whether a client exists or not.
                final ServiceType eventServiceType = serviceTypeRegistryService.findServiceType(event.getServiceType());
                if (eventServiceType.isRpcClient() && eventServiceType.isRecordStatistics()) {
                    if (toApplicationName.equals(event.getDestinationId())) {
                        if (checkResponseCondition(event.getEndElapsed(), event.hasException())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * WAS -> BACKEND (non-WAS)
     */
    private boolean wasToBackendFilter(List<SpanBo> transaction) {
        final List<SpanBo> fromNode = findFromNode(transaction);
        for (SpanBo span : fromNode) {
            final List<SpanEventBo> eventBoList = span.getSpanEventBoList();
            if (eventBoList == null) {
                continue;
            }
            for (SpanEventBo event : eventBoList) {
                final ServiceType eventServiceType = serviceTypeRegistryService.findServiceType(event.getServiceType());
                if (isToNode(event.getDestinationId(), eventServiceType)) {
                    if (checkResponseCondition(event.getEndElapsed(), event.hasException())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * WAS -> WAS
     */
    private boolean wasToWasFilter(List<SpanBo> transaction) {
        /*
         * WAS -> WAS
         * if destination is a "WAS", the span of src and dest may exist. need to check if be circular or not.
         * find src first. span (from, to) may exist more than one. so (spanId == parentSpanID) should be checked.
         */

        final List<SpanBo> fromSpanList = findFromNode(transaction);
        if (fromSpanList.isEmpty()) {
            // from span not found
            return false;
        }
        final List<SpanBo> toSpanList = findToNode(transaction);
        if (!toSpanList.isEmpty()) {

            // from -> to compare SpanId & pSpanId filter
            final boolean exactMatch = wasToWasExactMatch(fromSpanList, toSpanList);
            if (exactMatch)  {
                return true;
            }
        }
        if (isToAgentFilter()) {
            // fast skip. toAgent filtering condition exist.
            // url filter not available.
            return false;
        }

        // Check for url pattern should now be done on the caller side (from spans) as to spans are missing at this point
        if (!rpcUrlFilter.accept(fromSpanList)) {
            return false;
        }

        // if agent filter is FromAgentFilter or AcceptAgentFilter(agent filter is not selected), url filtering is available.
        return fromBaseFilter(fromSpanList);
    }

    /**
     * WAS -> Queue (virtual)
     * Should be the same as {@link #wasToBackendFilter}
     */
    private boolean wasToQueueFilter(List<SpanBo> transaction) {
        return wasToBackendFilter(transaction);
    }

    /**
     * Queue (virtual) -> WAS
     */
    private boolean queueToWasFilter(List<SpanBo> transaction) {
        final List<SpanBo> toNode = findToNode(transaction);
        logger.debug("matching toNode spans: {}", toNode);
        for (SpanBo span : toNode) {
            if (fromApplicationName.equals(span.getAcceptorHost())) {
                if (checkResponseCondition(span.getElapsed(), isError(span))) {
                    return true;
                }
            }
        }
        return false;
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
        for (SpanBo fromSpan : fromSpanList) {
            final List<SpanEventBo> eventBoList = fromSpan.getSpanEventBoList();
            if (eventBoList == null) {
                continue;
            }
            for (SpanEventBo event : eventBoList) {
                if (filterByRpcHints(rpcHintList, event)) {
                    return true;
                }
            }
        }
        return false;
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
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
                        return true;
                    }
                }
            }
        }
        return false;
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
                return true;
            }
        }
        return false;
    }

    private boolean includeUnknown(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isUnknown()) {
                return true;
            }
        }
        return false;
    }

    private boolean includeWas(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isWas()) {
                return true;
            }
        }
        return false;
    }

    private boolean includeQueue(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isQueue()) {
                return true;
            }
        }
        return false;
    }

    private boolean includeServiceType(List<ServiceType> serviceTypeList, ServiceType targetServiceType) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType == targetServiceType) {
                return true;
            }
        }
        return false;
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
        sb.append(", filterType=").append(filterType);
        sb.append(", rpcHintList=").append(rpcHintList);
        sb.append(", acceptURLFilter=").append(acceptURLFilter);
        sb.append('}');
        return sb.toString();
    }
}

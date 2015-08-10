/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.web.filter.agent.*;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class FromToResponseFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<ServiceType> fromServiceDescList;
    private final String fromApplicationName;

    private final List<ServiceType> toServiceDescList;
    private final String toApplicationName;

    private final ResponseTimeFilter responseTimeFilter;

    private final ExecutionType executionType;

    private final FilterHint filterHint;
    private final AgentFilter agentFilter;

    private final FilterType filterType;

    private final List<RpcHint> rpcHintList;

    public FromToResponseFilter(FilterDescriptor filterDescriptor, FilterHint filterHint) {
        if (filterDescriptor == null) {
            throw new NullPointerException("filter descriptor must not be null");
        }
        if (filterHint == null) {
            throw new NullPointerException("filterHint must not be null");
        }

        final String fromServiceType = filterDescriptor.getFromServiceType();
        this.fromServiceDescList = ServiceType.findDesc(fromServiceType);
        if (this.fromServiceDescList == null) {
            throw new IllegalArgumentException("fromServiceDescList not found. fromServiceType:" + fromServiceType);
        }

        this.fromApplicationName = filterDescriptor.getFromApplicationName();
        AssertUtils.assertNotNull(this.fromApplicationName, "fromApplicationName must not be null");


        final String toServiceType = filterDescriptor.getToServiceType();
        this.toServiceDescList = ServiceType.findDesc(toServiceType);
        if (toServiceDescList == null) {
            throw new IllegalArgumentException("toServiceDescList not found. toServiceDescList:" + toServiceType);
        }

        this.toApplicationName = filterDescriptor.getToApplicationName();
        AssertUtils.assertNotNull(this.toApplicationName, "toApplicationName must not be null");

        this.responseTimeFilter = createResponseTimeFilter(filterDescriptor);

        this.executionType = getExecutionType(filterDescriptor);

        this.filterHint = filterHint;
        AssertUtils.assertNotNull(this.filterHint, "filterHint must not be null");

        this.agentFilter = createAgentFilter(filterDescriptor);
        this.filterType = getFilterType();
        logger.debug("filterType:{}", filterType);

        this.rpcHintList = this.filterHint.getRpcHintList(fromApplicationName);
    }

    private ResponseTimeFilter createResponseTimeFilter(FilterDescriptor filterDescriptor) {
        final ResponseTimeFilterFactory factory = new ResponseTimeFilterFactory(filterDescriptor.getResponseFrom(), filterDescriptor.getResponseTo());
        return factory.createFilter();
    }

    private Long defaultLong(Long value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
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

    private AgentFilter createAgentFilter(FilterDescriptor filterDescriptor) {
        final String fromAgentName = filterDescriptor.getFromAgentName();
        final String toAgentName = filterDescriptor.getToAgentName();

        AgentFilterFactory factory = new AgentFilterFactory(fromAgentName, toAgentName);
        final AgentFilter agentFilter = factory.createFilter();
        logger.debug("agentFilter:{}", agentFilter);
        return agentFilter;
    }

    enum FilterType {
        WAS_TO_WAS,
        USER_TO_WAS,
        WAS_TO_UNKNOWN,
        WAS_TO_BACKEND,
        UNSUPPORTED;
    }

    enum ExecutionType {
        ALL,
        SUCCESS_ONLY,
        FAIL_ONLY;
    }

    public FilterType getFilterType() {
        if (includeWas(fromServiceDescList) && includeWas(toServiceDescList)) {
            return FilterType.WAS_TO_WAS;
        }
        if (includeServiceType(fromServiceDescList, ServiceType.USER) && includeWas(toServiceDescList)) {
            return FilterType.USER_TO_WAS;
        }
        if (includeWas(fromServiceDescList) && includeUnknown(toServiceDescList)) {
            return FilterType.WAS_TO_UNKNOWN;
        }
        // TODO toServiceDescList check logic not exist.
//        if (includeWas(fromServiceDescList) && isBackEnd????()) {
        if (includeWas(fromServiceDescList)) {
            return FilterType.WAS_TO_BACKEND;
        }
        return FilterType.UNSUPPORTED;
    }

    public boolean checkResponseCondition(long elapsed, boolean hasError) {
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

    private boolean filterAgentName(String fromAgentName, String toAgentName) {
        return this.agentFilter.accept(fromAgentName, toAgentName);
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
        for (SpanBo span : transaction) {
            if (span.isRoot() && includeServiceType(toServiceDescList, span.getServiceType()) && toApplicationName.equals(span.getApplicationId())) {
                return checkResponseCondition(span.getElapsed(), span.getErrCode() > 0)
                        && filterAgentName(null, span.getAgentId());
            }
        }
        return false;
    }

    private boolean wasToUnknownFilter(List<SpanBo> transaction) {
        /**
         * WAS -> UNKNOWN
         */
        for (SpanBo span : transaction) {
            if (isFromNode(span.getApplicationId(), span.getServiceType())) {
                List<SpanEventBo> eventBoList = span.getSpanEventBoList();
                if (eventBoList == null) {
                    continue;
                }

                for (SpanEventBo event : eventBoList) {
                    // check only whether a client exists or not.
                    if (event.getServiceType().isRpcClient() && event.getServiceType().isRecordStatistics()) {
                        if (toApplicationName.equals(event.getDestinationId())) {
                            return checkResponseCondition(event.getEndElapsed(), event.hasException());
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
        for (SpanBo span : transaction) {
            if (isFromNode(span.getApplicationId(), span.getServiceType())) {
                List<SpanEventBo> eventBoList = span.getSpanEventBoList();
                if (eventBoList == null) {
                    continue;
                }
                for (SpanEventBo event : eventBoList) {
                    if (isToNode(event.getDestinationId(), event.getServiceType())) {
                        return checkResponseCondition(event.getEndElapsed(), event.hasException())
                                && filterAgentName(span.getAgentId(), null);
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
        /**
         * WAS -> WAS
         * if destination is a "WAS", the span of src and dest may exists. need to check if be circular or not.
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
            for (SpanBo fromSpanBo : fromSpanList) {
                for (SpanBo toSpanBo : toSpanList) {
                    if (fromSpanBo == toSpanBo) {
                        // skip same object;
                        continue;
                    }
                    if (fromSpanBo.getSpanId() == toSpanBo.getParentSpanId()) {
                        final int elapsed = toSpanBo.getElapsed();
                        final boolean hasError = toSpanBo.getErrCode() > 0;
                        return checkResponseCondition(elapsed, hasError) && filterAgentName(fromSpanBo.getAgentId(), toSpanBo.getAgentId());
                    }
                }
            }
        }
        if ((agentFilter instanceof FromToAgentFilter) && (agentFilter instanceof ToAgentFilter)) {
            // fast skip. toAgent filtering condition exist.
            // url filter not available.
            return false;
        }

        if (!rpcHintList.isEmpty()) {
            return false;
        }
        // if agent filter is FromAgentFilter or AcceptAgentFilter(agent filter is not selected), url filtering is available.

        // url base filter.
        // exceptional case
        // 1. remote call fail
        // 2. span packet lost.
        for (SpanBo fromSpan : fromSpanList) {
            final List<SpanEventBo> eventBoList = fromSpan.getSpanEventBoList();
            if (eventBoList == null) {
                continue;
            }
            for (SpanEventBo event : eventBoList) {
                if (!event.getServiceType().isRpcClient()) {
                    continue;
                }

                if (!event.getServiceType().isRecordStatistics()) {
                    continue;
                }
                // check rpc call fail
                for (RpcHint rpcHint : rpcHintList) {
                    for (RpcType rpcType : rpcHint.getRpcTypeList()) {
                        boolean addressEquals = rpcType.getAddress().equals(event.getDestinationId());
                        boolean serviceTypeEquals = rpcType.getSpanEventServiceTypeCode() == event.getServiceType().getCode();
                        if (addressEquals && serviceTypeEquals) {
                            return checkResponseCondition(event.getEndElapsed(), event.hasException()) && agentFilter.accept(fromSpan.getAgentId(), null);
                        }
                    }
                }
            }
        }
        return false;

    }

    private List<SpanBo> findFromNode(List<SpanBo> transaction) {
        return findNode(transaction, fromApplicationName, fromServiceDescList);
    }

    private List<SpanBo> findToNode(List<SpanBo> transaction) {
        return findNode(transaction, toApplicationName, toServiceDescList);
    }


    private List<SpanBo> findNode(List<SpanBo> nodeList, String findApplicationName, List<ServiceType> findServiceCode) {
        List<SpanBo> findList = null;
        for (SpanBo span : nodeList) {
            if (findApplicationName.equals(span.getApplicationId()) && includeServiceType(findServiceCode, span.getServiceType())) {
                if (findList == null) {
                    findList = new ArrayList<>();
                }
                findList.add(span);
            }
        }
        if (findList == null) {
            return Collections.emptyList();
        }
        return findList;
    }


    private boolean isFromNode(String applicationName, ServiceType serviceType) {
        return this.fromApplicationName.equals(applicationName) && includeServiceType(this.fromServiceDescList, serviceType);
    }

    private boolean isToNode(String applicationId, ServiceType serviceType) {
        return this.toApplicationName.equals(applicationId) && includeServiceType(this.toServiceDescList, serviceType);
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
        final StringBuilder sb = new StringBuilder("FromToResponseFilter{");
        sb.append("fromServiceDescList=").append(fromServiceDescList);
        sb.append(", fromApplicationName='").append(fromApplicationName).append('\'');
        sb.append(", toServiceDescList=").append(toServiceDescList);
        sb.append(", toApplicationName='").append(toApplicationName).append('\'');
        sb.append(", responseTimeFilter=").append(responseTimeFilter);
        sb.append(", executionType=").append(executionType);
        sb.append(", hintFilter=").append(filterHint);
        sb.append(", agentFilter=").append(agentFilter);
        sb.append('}');
        return sb.toString();
    }
}

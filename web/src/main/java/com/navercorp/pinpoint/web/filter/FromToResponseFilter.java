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

import java.util.List;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.rpc.util.AssertUtils;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.FilterDescriptor;
import com.navercorp.pinpoint.web.filter.FilterHint;
import com.navercorp.pinpoint.web.filter.agent.AgentFilter;
import com.navercorp.pinpoint.web.filter.agent.AgentFilterFactory;
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

    private final List<ServiceType> fromServiceCode;
    private final String fromApplicationName;

    private final List<ServiceType> toServiceCode;
    private final String toApplicationName;

    private final ResponseTimeFilter responseTimeFilter;

    private final ExecutionType executionType;

    private final FilterHint filterHint;
    private final AgentFilter agentFilter;

    private final FilterType filterType;

    public FromToResponseFilter(FilterDescriptor filterDescriptor, FilterHint filterHint) {
        if (filterDescriptor == null) {
            throw new NullPointerException("filter descriptor must not be null");
        }
        if (filterHint == null) {
            throw new NullPointerException("filterHint must not be null");
        }

        final String fromServiceType = filterDescriptor.getFromServiceType();
        this.fromServiceCode = ServiceType.findDesc(fromServiceType);
        if (this.fromServiceCode == null) {
            throw new IllegalArgumentException("fromServiceCode not found. fromServiceType:" + fromServiceType);
        }

        this.fromApplicationName = filterDescriptor.getFromApplicationName();
        AssertUtils.assertNotNull(this.fromApplicationName, "fromApplicationName must not be null");


        final String toServiceType = filterDescriptor.getToServiceType();
        this.toServiceCode = ServiceType.findDesc(toServiceType);
        if (toServiceCode == null) {
            throw new IllegalArgumentException("toServiceCode not found. toServiceCode:" + toServiceType);
        }

        this.toApplicationName = filterDescriptor.getToApplicationName();
        AssertUtils.assertNotNull(this.toApplicationName, "toApplicationName must not be null");

        this.responseTimeFilter = createResponseTimeFilter(filterDescriptor);

        this.executionType = getExecutionType(filterDescriptor);

        this.filterHint = filterHint;
        AssertUtils.assertNotNull(this.filterHint, "filterHint must not be null");

        this.agentFilter = createAgentFilter(filterDescriptor);
        this.filterType = getFilterType();
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
        if (includeWas(fromServiceCode) && includeWas(toServiceCode)) {
            return FilterType.WAS_TO_WAS;
        }
        if (includeServiceType(fromServiceCode, ServiceType.USER) && includeWas(toServiceCode)) {
            return FilterType.USER_TO_WAS;
        }
        if (includeWas(fromServiceCode) && includeUnknown(toServiceCode)) {
            return FilterType.WAS_TO_UNKNOWN;
        }
        // TODO toServiceCode check logic not exist.
//        if (includeWas(fromServiceCode) && isBackEnd????()) {
        if (includeWas(fromServiceCode)) {
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
        logger.debug("User -> Was Filter");
        for (SpanBo span : transaction) {
            if (span.isRoot() && includeServiceType(toServiceCode, span.getServiceType()) && toApplicationName.equals(span.getApplicationId())) {
                return checkResponseCondition(span.getElapsed(), span.getErrCode() > 0)
                        && filterAgentName(null, span.getAgentId());
            }
        }
        return false;
    }

    private boolean wasToUnknownFilter(List<SpanBo> transaction) {
        logger.debug("Was -> Unknown Filter");
        /**
         * WAS -> UNKNOWN
         */
        for (SpanBo span : transaction) {
            if (includeServiceType(fromServiceCode, span.getServiceType()) && fromApplicationName.equals(span.getApplicationId())) {
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
        logger.debug("WAS -> BACKEND (non-WAS)");
        for (SpanBo span : transaction) {
            if (includeServiceType(fromServiceCode, span.getServiceType()) && fromApplicationName.equals(span.getApplicationId())) {
                List<SpanEventBo> eventBoList = span.getSpanEventBoList();
                if (eventBoList == null) {
                    continue;
                }
                for (SpanEventBo event : eventBoList) {
                    if (includeServiceType(toServiceCode, event.getServiceType()) && toApplicationName.equals(event.getDestinationId())) {
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
        if (filterHint.containApplicationHint(toApplicationName)) {
            logger.debug("WAS -> WAS hint Filter");
            for (SpanBo srcSpan : transaction) {
                List<SpanEventBo> eventBoList = srcSpan.getSpanEventBoList();
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

                    if (!filterHint.containApplicationEndpoint(toApplicationName, event.getDestinationId(), event.getServiceType().getCode())) {
                        continue;
                    }

                    return checkResponseCondition(event.getEndElapsed(), event.hasException());

                    // FIXME below code should be added for agent filter to work properly
                    // && checkPinPointAgentName(srcSpan.getAgentId(), destSpan.getAgentId());
                }
            }
        } else {
            logger.debug("WAS -> WAS nonhint Filter");
            /**
             * codes before hintFilter has been added.
             * if problems happen because of hintFilter, don't use hintFilter at front end (UI) or use below code in order to work properly.
             */
            for (SpanBo srcSpan : transaction) {
                if (includeServiceType(fromServiceCode, srcSpan.getServiceType()) && fromApplicationName.equals(srcSpan.getApplicationId())) {
                    // find dest of src.
                    for (SpanBo destSpan : transaction) {
                        if (destSpan.getParentSpanId() != srcSpan.getSpanId()) {
                            continue;
                        }

                        if (includeServiceType(toServiceCode, destSpan.getServiceType()) && toApplicationName.equals(destSpan.getApplicationId())) {
                            return checkResponseCondition(destSpan.getElapsed(), destSpan.getErrCode() > 0) && filterAgentName(srcSpan.getAgentId(), destSpan.getAgentId());
                        }
                    }
                }
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
        sb.append("fromServiceCode=").append(fromServiceCode);
        sb.append(", fromApplicationName='").append(fromApplicationName).append('\'');
        sb.append(", toServiceCode=").append(toServiceCode);
        sb.append(", toApplicationName='").append(toApplicationName).append('\'');
        sb.append(", responseTimeFilter=").append(responseTimeFilter);
        sb.append(", executionType=").append(executionType);
        sb.append(", hintFilter=").append(filterHint);
        sb.append(", agentFilter=").append(agentFilter);
        sb.append('}');
        return sb.toString();
    }
}

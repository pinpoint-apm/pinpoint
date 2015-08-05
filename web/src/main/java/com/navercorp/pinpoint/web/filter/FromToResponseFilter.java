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
import com.navercorp.pinpoint.web.filter.agent.AgentFilter;
import com.navercorp.pinpoint.web.filter.agent.AgentFilterFactory;
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

    private final Long fromResponseTime;
    private final Long toResponseTime;
    private final Boolean includeFailed;

    private final FilterHint filterHint;
    private final AgentFilter agentFilter;

    public FromToResponseFilter(FilterDescriptor filterDescriptor, FilterHint filterHint) {
        if (filterDescriptor == null) {
            throw new NullPointerException("filter descriptor must not be null");
        }

        final String fromServiceType = filterDescriptor.getFromServiceType();
        this.fromServiceCode = ServiceType.findDesc(fromServiceType);
        if (this.fromServiceCode == null) {
            throw new IllegalArgumentException("fromServiceCode not found. fromServiceType:" + fromServiceType);
        }

        this.fromApplicationName = filterDescriptor.getFromApplicationName();
        AssertUtils.assertNotNull(this.fromApplicationName, "fromApplicationName must not be null");
//        this.fromAgentName = filterDescriptor.getFromAgentName();


        final String toServiceType = filterDescriptor.getToServiceType();
        this.toServiceCode = ServiceType.findDesc(toServiceType);
        if (toServiceCode == null) {
            throw new IllegalArgumentException("toServiceCode not found. toServiceCode:" + toServiceType);
        }


        this.toApplicationName = filterDescriptor.getToApplicationName();
        AssertUtils.assertNotNull(this.toApplicationName, "toApplicationName must not be null");
//        this.toAgentName = filterDescriptor.getToAgentName();

        this.fromResponseTime = filterDescriptor.getResponseFrom();
        this.toResponseTime = filterDescriptor.getResponseTo();

        this.includeFailed = filterDescriptor.getIncludeException();

        if (filterHint == null) {
            throw new NullPointerException("hintFilter must not be null");
        }
        this.filterHint = filterHint;

        this.agentFilter = createAgentFilter(filterDescriptor);

    }

    private AgentFilter createAgentFilter(FilterDescriptor filterDescriptor) {
        final String fromAgentName = filterDescriptor.getFromAgentName();
        final String toAgentName = filterDescriptor.getToAgentName();

        AgentFilterFactory factory = new AgentFilterFactory(fromAgentName, toAgentName);
        final AgentFilter agentFilter = factory.createFilter();
        logger.debug("agentFilter:{}", agentFilter);
        return agentFilter;
    }

    private boolean checkResponseCondition(long elapsed, boolean hasError) {
        boolean result = true;
        if (fromResponseTime != null && toResponseTime != null) {
            result &= (elapsed >= fromResponseTime) && (elapsed <= toResponseTime);
        }
        if (includeFailed != null) {
            if (includeFailed) {
                result &= hasError;
            } else {
                result &= !hasError;
            }
        }
        return result;
    }

    private boolean filterAgentName(String fromAgentName, String toAgentName) {
        return this.agentFilter.accept(fromAgentName, toAgentName);
    }

    @Override
    public boolean include(List<SpanBo> transaction) {
        if (includeServiceType(fromServiceCode, ServiceType.USER)) {
            /**
             * USER -> WAS
             */
            for (SpanBo span : transaction) {
                if (span.isRoot() && includeServiceType(toServiceCode, span.getServiceType()) && toApplicationName.equals(span.getApplicationId())) {
                    return checkResponseCondition(span.getElapsed(), span.getErrCode() > 0)
                            && filterAgentName(null, span.getAgentId());
                }
            }
        } else if (includeUnknown(toServiceCode)) {
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
        } else if (includeWas(toServiceCode)) {
            /**
             * WAS -> WAS
             * if destination is a "WAS", the span of src and dest may exists. need to check if be circular or not.
             * find src first. span (from, to) may exist more than one. so (spanId == parentSpanID) should be checked.
             */
            if (filterHint.containApplicationHint(toApplicationName)) {
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
        } else {
            /**
             * WAS -> BACKEND (non-WAS)
             */
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
        sb.append(", fromResponseTime=").append(fromResponseTime);
        sb.append(", toResponseTime=").append(toResponseTime);
        sb.append(", includeFailed=").append(includeFailed);
        sb.append(", hintFilter=").append(filterHint);
        sb.append(", agentFilter=").append(agentFilter);
        sb.append('}');
        return sb.toString();
    }
}

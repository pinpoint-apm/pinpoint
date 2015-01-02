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

/**
 * 
 * @author netspider
 * 
 */
public class FromToResponseFilter implements Filter {

    private final List<ServiceType> fromServiceCode;
    private final String fromApplicationName;
    private final String fromAgentName;

    private final List<ServiceType> toServiceCode;
    private final String toApplicationName;
    private final String toAgentName;

    private final Long fromResponseTime;
    private final Long toResponseTime;
    private final Boolean includeFailed;

    private final FilterHint hint;

    public FromToResponseFilter(FilterDescriptor filterDescriptor, FilterHint hint) {
        if (filterDescriptor == null) {
            throw new NullPointerException("filter descriptor must not be null");
        }

        String fromServiceType = filterDescriptor.getFromServiceType();
        String fromApplicationName = filterDescriptor.getFromApplicationName();
        String fromAgentName = filterDescriptor.getFromAgentName();
        String toServiceType = filterDescriptor.getToServiceType();
        String toApplicationName = filterDescriptor.getToApplicationName();
        String toAgentName = filterDescriptor.getToAgentName();
        Long fromResponseTime = filterDescriptor.getResponseFrom();
        Long toResponseTime = filterDescriptor.getResponseTo();
        Boolean includeFailed = filterDescriptor.getIncludeException();

        if (fromApplicationName == null) {
            throw new NullPointerException("fromApplicationName must not be null");
        }
        if (toApplicationName == null) {
            throw new NullPointerException("toApplicationName must not be null");
        }

        this.fromServiceCode = ServiceType.findDesc(fromServiceType);
        if (fromServiceCode == null) {
            throw new IllegalArgumentException("fromServiceCode not found. fromServiceType:" + fromServiceType);
        }
        this.fromApplicationName = fromApplicationName;
        this.fromAgentName = fromAgentName;

        this.toServiceCode = ServiceType.findDesc(toServiceType);
        if (toServiceCode == null) {
            throw new IllegalArgumentException("toServiceCode not found. toServiceCode:" + toServiceType);
        }
        this.toApplicationName = toApplicationName;
        this.toAgentName = toAgentName;

        this.fromResponseTime = fromResponseTime;
        this.toResponseTime = toResponseTime;
        this.includeFailed = includeFailed;

        if (hint == null) {
            throw new NullPointerException("hint must not be null");
        }
        this.hint = hint;
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

    private boolean checkPinPointAgentName(String fromAgentName, String toAgentName) {
        if (this.fromAgentName == null && this.toAgentName == null) {
            return true;
        }

        boolean result = true;

        if (this.fromAgentName != null) {
            result &= this.fromAgentName.equals(fromAgentName);
        }

        if (this.toAgentName != null) {
            result &= this.toAgentName.equals(toAgentName);
        }

        return result;
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
                            && checkPinPointAgentName(null, span.getAgentId());
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
                        if (event.getServiceType().isRpcClient() && toApplicationName.equals(event.getDestinationId())) {
                            return checkResponseCondition(event.getEndElapsed(), event.hasException());
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
            if (hint.containApplicationHint(toApplicationName)) {
                for (SpanBo srcSpan : transaction) {
                    List<SpanEventBo> eventBoList = srcSpan.getSpanEventBoList();
                    if (eventBoList == null) {
                        continue;
                    }
                    for (SpanEventBo event : eventBoList) {
                        if (!event.getServiceType().isRpcClient()) {
                            continue;
                        }

                        if (!hint.containApplicationEndpoint(toApplicationName, event.getDestinationId(), event.getServiceType().getCode())) {
                            continue;
                        }

                        return checkResponseCondition(event.getEndElapsed(), event.hasException());

                        // FIXME below code should be added for agent filter to work properly
                        // && checkPinPointAgentName(srcSpan.getAgentId(), destSpan.getAgentId());
                    }
                }
            } else {
                /**
                 * codes before hint has been added.
                 * if problems happen because of hint, don't use hint at front end (UI) or use below code in order to work properly.
                 */
                for (SpanBo srcSpan : transaction) {
                    if (includeServiceType(fromServiceCode, srcSpan.getServiceType()) && fromApplicationName.equals(srcSpan.getApplicationId())) {
                        // find dest of src.
                        for (SpanBo destSpan : transaction) {
                            if (destSpan.getParentSpanId() != srcSpan.getSpanId()) {
                                continue;
                            }

                            if (includeServiceType(toServiceCode, destSpan.getServiceType()) && toApplicationName.equals(destSpan.getApplicationId())) {
                                return checkResponseCondition(destSpan.getElapsed(), destSpan.getErrCode() > 0) && checkPinPointAgentName(srcSpan.getAgentId(), destSpan.getAgentId());
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
                                    && checkPinPointAgentName(span.getAgentId(), null);
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
        return "FromToResponseFilter [fromServiceCode=" + fromServiceCode + ", fromApplicationName=" + fromApplicationName + ", fromAgentName=" + fromAgentName + ", toServiceCode=" + toServiceCode + ", toApplicationName=" + toApplicationName + ", toAgentName=" + toAgentName + ", fromResponseTime=" + fromResponseTime + ", toResponseTime=" + toResponseTime + ", includeFailed=" + includeFailed + "]";
    }
}

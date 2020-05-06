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

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.filter.agent.AgentFilter;
import com.navercorp.pinpoint.web.filter.agent.DefaultAgentFilter;
import com.navercorp.pinpoint.web.filter.agent.SkipAgentFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yjqg6666
 */
public class ApplicationFilter implements Filter<SpanBo> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<ServiceType> serviceDescList;

    private final String applicationName;

    private final ResponseTimeFilter responseTimeFilter;

    private final ExecutionType executionType;

    private final FilterHint filterHint;

    private final AgentFilter agentFilter;

    private final List<RpcHint> rpcHintList;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    private final URLPatternFilter acceptURLFilter;

    public ApplicationFilter(FilterDescriptor filterDescriptor, FilterHint filterHint, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {
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

        final String serviceType = filterDescriptor.getServiceType();
        this.serviceDescList = serviceTypeRegistryService.findDesc(serviceType);
        if (this.serviceDescList == null) {
            throw new IllegalArgumentException("serviceDescList not found. serviceType:" + serviceType);
        }

        this.applicationName = filterDescriptor.getApplicationName();
        Assert.notNull(this.applicationName, "applicationName must not be null");

        this.responseTimeFilter = createResponseTimeFilter(filterDescriptor);

        this.executionType = getExecutionType(filterDescriptor);

        this.filterHint = filterHint;
        Assert.notNull(this.filterHint, "filterHint must not be null");

        logger.debug("agentFilter:agentId:{}", filterDescriptor.getAgentName());
        this.agentFilter = createAgentFilter(filterDescriptor.getAgentName());

        this.rpcHintList = this.filterHint.getRpcHintList(applicationName);

        this.acceptURLFilter = createAcceptUrlFilter(filterDescriptor);
        logger.info("acceptURLFilter:{}", acceptURLFilter);
    }

    @Override
    public boolean include(List<SpanBo> spanBoList) {
        final List<SpanBo> spanList = findApplicationNode(spanBoList);
        if (spanList.isEmpty()) {
            logger.debug("Find no application node, application name:{}, agent filter:{}", applicationName, agentFilter);
            return false;
        }

        if (!acceptURLFilter.accept(spanList)) {
            return false;
        }

        return responseFilter(spanList);
    }

    private AgentFilter createAgentFilter(String agentId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(agentId)) {
            return SkipAgentFilter.SKIP_FILTER;
        }
        return new DefaultAgentFilter(agentId);
    }

    private URLPatternFilter createAcceptUrlFilter(FilterDescriptor filterDescriptor) {
        if (StringUtils.isEmpty(filterDescriptor.getUrlPattern())) {
            return new BypassURLPatternFilter();
        }
        return new AcceptUrlFilter(filterDescriptor.getUrlPattern());
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

    private boolean responseFilter(List<SpanBo> spanList) {
        for (SpanBo span : spanList) {
            boolean gotError = isErrorSpan(span) || spanEventHaveException(span);
            if (checkResponseCondition(span.getElapsed(), gotError)) {
                return true;
            }
        }
        return false;
    }

    private boolean isErrorSpan(SpanBo span) {
        return span.getErrCode() > 0;
    }

    private boolean spanEventHaveException(SpanBo spanBo) {
        final List<SpanEventBo> eventBoList = spanBo.getSpanEventBoList();
        if (eventBoList == null) {
            return false;
        }
        for (SpanEventBo event : eventBoList) {
            if (event.hasException()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkResponseCondition(long elapsed, boolean hasError) {
        if (responseTimeFilter.accept(elapsed) == ResponseTimeFilter.REJECT) {
            return false;
        }
        return checkExecutionError(hasError);
    }

    private boolean checkExecutionError(boolean hasError) {
        switch (executionType) {
            case ALL: {
                return true;
            }
            case FAIL_ONLY: {
                return hasError;
            }
            case SUCCESS_ONLY: {
                return !hasError;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported ExecutionType:" + executionType);
            }
        }
    }

    private List<SpanBo> findApplicationNode(List<SpanBo> transaction) {
        return findNode(transaction, applicationName, serviceDescList, agentFilter);
    }

    private List<SpanBo> findNode(List<SpanBo> spanBoList, String findApplicationName, List<ServiceType> findServiceCode, AgentFilter agentFilter) {
        List<SpanBo> findList = null;
        for (SpanBo spanBo : spanBoList) {
            final ServiceType applicationServiceType = serviceTypeRegistryService.findServiceType(spanBo.getApplicationServiceType());
            if (findApplicationName.equals(spanBo.getApplicationId()) && includeServiceType(findServiceCode, applicationServiceType)) {
                if (agentFilter.accept(spanBo.getAgentId())) {
                    if (findList == null) {
                        findList = new ArrayList<>();
                    }
                    findList.add(spanBo);
                }
            }
        }
        if (findList == null) {
            return Collections.emptyList();
        }
        return findList;
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
        sb.append("serviceDescList=").append(serviceDescList);
        sb.append(", applicationName='").append(applicationName).append('\'');
        sb.append(", responseTimeFilter=").append(responseTimeFilter);
        sb.append(", executionType=").append(executionType);
        sb.append(", filterHint=").append(filterHint);
        sb.append(", agentFilter=").append(agentFilter);
        sb.append(", rpcHintList=").append(rpcHintList);
        sb.append(", acceptURLFilter=").append(acceptURLFilter);
        sb.append('}');
        return sb.toString();
    }
}

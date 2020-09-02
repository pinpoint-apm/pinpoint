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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.filter.agent.AgentFilter;
import com.navercorp.pinpoint.web.filter.agent.AgentFilterFactory;
import com.navercorp.pinpoint.web.filter.responsetime.DefaultExecutionTypeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ExecutionTypeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilterFactory;
import com.navercorp.pinpoint.web.filter.responsetime.SpanResponseConditionFilter;
import com.navercorp.pinpoint.web.filter.transaction.NodeContext;
import com.navercorp.pinpoint.web.filter.transaction.SpanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author yjqg6666
 */
public class ApplicationFilter implements Filter<List<SpanBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<ServiceType> serviceDescList;

    private final FilterDescriptor.SelfNode selfNode;

    private final Filter<SpanBo> spanResponseConditionFilter;

    private final AgentFilter agentFilter;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final URLPatternFilter acceptURLFilter;

    public ApplicationFilter(FilterDescriptor filterDescriptor, ServiceTypeRegistryService serviceTypeRegistryService) {
        Objects.requireNonNull(filterDescriptor, "filterDescriptor");

        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");

        this.selfNode = filterDescriptor.getSelfNode();

        String applicationName = selfNode.getApplicationName();
        Objects.requireNonNull(applicationName, "applicationName");

        final String serviceType = selfNode.getServiceType();
        this.serviceDescList = findDesc(serviceType, "applicationServiceType");

        ResponseTimeFilter responseTimeFilter = createResponseTimeFilter(filterDescriptor.getResponseTime());

        ExecutionTypeFilter executionErrorFilter = newExecutionErrorFilter(filterDescriptor.getOption());
        this.spanResponseConditionFilter = new SpanResponseConditionFilter(responseTimeFilter, executionErrorFilter, SpanResponseConditionFilter.ErrorCheck.SPAN_AND_SPANEVENT);


        logger.debug("agentFilter:agentId:{}", selfNode.getAgentId());
        this.agentFilter = AgentFilterFactory.createAgentFilter(selfNode.getAgentId());

        this.acceptURLFilter = createAcceptUrlFilter(filterDescriptor.getOption());
        logger.info("acceptURLFilter:{}", acceptURLFilter);
    }

    private List<ServiceType> findDesc(String serviceType, String typeName) {
        List<ServiceType> toServiceDescList = serviceTypeRegistryService.findDesc(serviceType);
        if (toServiceDescList == null) {
            throw new IllegalArgumentException(typeName + " not found. toServiceDescList:" + serviceType);
        }
        return toServiceDescList;
    }

    @Override
    public boolean include(List<SpanBo> spanBoList) {
        SpanContext spanContext = new SpanContext(spanBoList, serviceTypeRegistryService);
        NodeContext nodeContext = new NodeContext(spanContext, selfNode.getApplicationName(), serviceDescList, agentFilter);

        com.navercorp.pinpoint.web.filter.transaction.ApplicationFilter filter = new com.navercorp.pinpoint.web.filter.transaction.ApplicationFilter(spanResponseConditionFilter, acceptURLFilter);
        return filter.include(nodeContext);
    }

    private URLPatternFilter createAcceptUrlFilter(FilterDescriptor.Option option) {
        if (StringUtils.isEmpty(option.getUrlPattern())) {
            return URLPatternFilter::filterAccept;
        }
        return new AcceptUrlFilter(option.getUrlPattern());
    }

    private ResponseTimeFilter createResponseTimeFilter(FilterDescriptor.ResponseTime responseTime) {
        final ResponseTimeFilterFactory factory = new ResponseTimeFilterFactory(responseTime.getFromResponseTime(), responseTime.getToResponseTime());
        return factory.createFilter();
    }


    private ExecutionTypeFilter newExecutionErrorFilter(FilterDescriptor.Option option) {
        return DefaultExecutionTypeFilter.newExecutionTypeFilter(option.getIncludeException());
    }



}

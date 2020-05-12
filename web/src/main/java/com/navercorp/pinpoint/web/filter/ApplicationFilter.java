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
import com.navercorp.pinpoint.web.filter.transaction.SpanContainer;
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

    private final String applicationName;

    private final Filter<SpanBo> spanResponseConditionFilter;

    private final AgentFilter agentFilter;

    private final ServiceTypeRegistryService serviceTypeRegistryService;


    private final URLPatternFilter acceptURLFilter;

    public ApplicationFilter(FilterDescriptor filterDescriptor, ServiceTypeRegistryService serviceTypeRegistryService) {
        Objects.requireNonNull(filterDescriptor, "filterDescriptor");

        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");

        final String serviceType = filterDescriptor.getServiceType();
        this.serviceDescList = serviceTypeRegistryService.findDesc(serviceType);
        if (this.serviceDescList == null) {
            throw new IllegalArgumentException("serviceDescList not found. serviceType:" + serviceType);
        }

        this.applicationName = filterDescriptor.getApplicationName();
        Objects.requireNonNull(applicationName, "applicationName");

        ResponseTimeFilter responseTimeFilter = createResponseTimeFilter(filterDescriptor);

        ExecutionTypeFilter executionErrorFilter = newExecutionErrorFilter(filterDescriptor);
        this.spanResponseConditionFilter = new SpanResponseConditionFilter(responseTimeFilter, executionErrorFilter, SpanResponseConditionFilter.ErrorCheck.SPAN_AND_SPANEVENT);


        logger.debug("agentFilter:agentId:{}", filterDescriptor.getAgentName());
        this.agentFilter = AgentFilterFactory.createAgentFilter(filterDescriptor.getAgentName());

        this.acceptURLFilter = createAcceptUrlFilter(filterDescriptor);
        logger.info("acceptURLFilter:{}", acceptURLFilter);
    }

    @Override
    public boolean include(List<SpanBo> spanBoList) {
        SpanContainer node = new SpanContainer(spanBoList, serviceTypeRegistryService);
        final List<SpanBo> spanList = findApplicationNode(node);
        if (spanList.isEmpty()) {
            logger.debug("Find no application node, application name:{}, agent filter:{}", applicationName, agentFilter);
            return false;
        }

        if (!acceptURLFilter.accept(spanList)) {
            return false;
        }

        return responseFilter(spanList);
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


    private ExecutionTypeFilter newExecutionErrorFilter(FilterDescriptor filterDescriptor) {
        final Boolean includeException = filterDescriptor.getIncludeException();
        return DefaultExecutionTypeFilter.newExecutionTypeFilter(includeException);
    }

    private boolean responseFilter(List<SpanBo> spanList) {
        for (SpanBo span : spanList) {
            if (this.spanResponseConditionFilter.include(span)) {
                return true;
            }
        }
        return false;
    }


    private List<SpanBo> findApplicationNode(SpanContainer spanContainer) {
        return spanContainer.findNode(applicationName, serviceDescList, agentFilter);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LinkFilter{");
        sb.append("serviceDescList=").append(serviceDescList);
        sb.append(", applicationName='").append(applicationName).append('\'');
        sb.append(", spanResponseConditionFilter=").append(spanResponseConditionFilter);
        sb.append(", agentFilter=").append(agentFilter);
        sb.append(", acceptURLFilter=").append(acceptURLFilter);
        sb.append('}');
        return sb.toString();
    }
}

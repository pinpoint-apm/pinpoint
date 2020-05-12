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

import java.util.List;
import java.util.Objects;


import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.filter.agent.AgentFilter;
import com.navercorp.pinpoint.web.filter.agent.AgentFilterFactory;
import com.navercorp.pinpoint.web.filter.responsetime.DefaultExecutionTypeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ExecutionTypeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilter;
import com.navercorp.pinpoint.web.filter.responsetime.ResponseTimeFilterFactory;
import com.navercorp.pinpoint.web.filter.responsetime.SpanEventResponseConditionFilter;
import com.navercorp.pinpoint.web.filter.responsetime.SpanResponseConditionFilter;
import com.navercorp.pinpoint.web.filter.transaction.LinkFilterContext;
import com.navercorp.pinpoint.web.filter.transaction.QueueToWasFilter;
import com.navercorp.pinpoint.web.filter.transaction.SpanContainer;
import com.navercorp.pinpoint.web.filter.transaction.UserToWasFilter;
import com.navercorp.pinpoint.web.filter.transaction.WasToBackendFilter;
import com.navercorp.pinpoint.web.filter.transaction.WasToQueueFilter;
import com.navercorp.pinpoint.web.filter.transaction.WasToUnknownFilter;
import com.navercorp.pinpoint.web.filter.transaction.WasToWasFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author netspider
 * @author emeroad
 */
public class LinkFilter implements Filter<List<SpanBo>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<ServiceType> fromServiceDescList;
    private final String fromApplicationName;

    private final List<ServiceType> toServiceDescList;
    private final String toApplicationName;

    private final Filter<SpanBo> spanResponseConditionFilter;

    private final Filter<SpanEventBo> spanEventResponseConditionFilter;

    private final FilterHint filterHint;

    private final AgentFilterFactory agentFilterFactory;
    private final AgentFilter fromAgentFilter;
    private final AgentFilter toAgentFilter;

    private final Filter<LinkFilterContext> filter;

    private final List<RpcHint> rpcHintList;

    private final ServiceTypeRegistryService serviceTypeRegistryService;


    private final URLPatternFilter acceptURLFilter;
    private final URLPatternFilter rpcUrlFilter;

    public LinkFilter(FilterDescriptor filterDescriptor, FilterHint filterHint, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {
        Objects.requireNonNull(filterDescriptor, "filterDescriptor");

        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");

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

        final ResponseTimeFilter responseTimeFilter = createResponseTimeFilter(filterDescriptor);
        final ExecutionTypeFilter executionErrorFilter = newExecutionErrorFilter(filterDescriptor);
        this.spanResponseConditionFilter = newSpanResponseConditionFilter(responseTimeFilter, executionErrorFilter);
        this.spanEventResponseConditionFilter = newSpanEventResponseConditionFilter(responseTimeFilter, executionErrorFilter);

        this.filterHint = Objects.requireNonNull(filterHint, "filterHint");

        final String fromAgentName = filterDescriptor.getFromAgentName();
        final String toAgentName = filterDescriptor.getToAgentName();

        this.agentFilterFactory = new AgentFilterFactory(fromAgentName, toAgentName);
        logger.debug("agentFilterFactory:{}", agentFilterFactory);
        this.fromAgentFilter = agentFilterFactory.createFromAgentFilter();
        this.toAgentFilter = agentFilterFactory.createToAgentFilter();

        this.rpcHintList = this.filterHint.getRpcHintList(toApplicationName);

        // TODO fix : fromSpan base rpccall filter
        this.acceptURLFilter = createAcceptUrlFilter(filterDescriptor);
        this.rpcUrlFilter = createRpcUrlFilter(filterDescriptor, annotationKeyRegistryService);
        logger.info("acceptURLFilter:{}", acceptURLFilter);

        this.filter = resolveFilter();
        logger.info("filter:{}", filter);
    }

    private Filter<SpanBo> newSpanResponseConditionFilter(ResponseTimeFilter responseTimeFilter, ExecutionTypeFilter executionErrorFilter) {
        return new SpanResponseConditionFilter(responseTimeFilter, executionErrorFilter, SpanResponseConditionFilter.ErrorCheck.SPAN);

    }
    private Filter<SpanEventBo> newSpanEventResponseConditionFilter(ResponseTimeFilter responseTimeFilter, ExecutionTypeFilter executionErrorFilter) {
        return new SpanEventResponseConditionFilter(responseTimeFilter, executionErrorFilter);
    }

    private URLPatternFilter createAcceptUrlFilter(FilterDescriptor filterDescriptor) {
        final String urlPattern = filterDescriptor.getUrlPattern();
        if (StringUtils.isEmpty(urlPattern)) {
            return new BypassURLPatternFilter();
        }
        // TODO remove decode
        return new AcceptUrlFilter(urlPattern);
    }

    private URLPatternFilter createRpcUrlFilter(FilterDescriptor filterDescriptor, AnnotationKeyRegistryService annotationKeyRegistryService) {
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


    private ExecutionTypeFilter newExecutionErrorFilter(FilterDescriptor filterDescriptor) {
        final Boolean includeException = filterDescriptor.getIncludeException();
        return DefaultExecutionTypeFilter.newExecutionTypeFilter(includeException);
    }

    private Filter<LinkFilterContext> resolveFilter() {
        if (includeWas(fromServiceDescList) && includeWas(toServiceDescList)) {
            return new WasToWasFilter(spanResponseConditionFilter, spanEventResponseConditionFilter,
                    agentFilterFactory, rpcUrlFilter, acceptURLFilter, rpcHintList);
        }
        if (includeUser(fromServiceDescList) && includeWas(toServiceDescList)) {
            return new UserToWasFilter(spanResponseConditionFilter, acceptURLFilter);
        }
        if (includeWas(fromServiceDescList) && includeUnknown(toServiceDescList)) {
            return new WasToUnknownFilter(spanEventResponseConditionFilter, rpcUrlFilter);
        }
        if (includeWas(fromServiceDescList) && includeQueue(toServiceDescList)) {
            return new WasToQueueFilter(spanEventResponseConditionFilter);
        }
        if (includeQueue(fromServiceDescList) && includeWas(toServiceDescList)) {
            return new QueueToWasFilter(spanResponseConditionFilter, acceptURLFilter);
        }
        // TODO toServiceDescList check logic not exist.
//        if (includeWas(fromServiceDescList) && isBackEnd????()) {
        if (includeWas(fromServiceDescList)) {
            return new WasToBackendFilter(spanEventResponseConditionFilter);
        }
        logger.warn("unsupported filter type:{}", this);
        throw new IllegalArgumentException("filter resolve fail:" + this);
    }


    @Override
    public boolean include(List<SpanBo> transaction) {
        SpanContainer spanContainer = new SpanContainer(transaction, serviceTypeRegistryService);
        LinkFilterContext linkFilterContext = new LinkFilterContext(spanContainer,
                fromApplicationName, fromServiceDescList, fromAgentFilter,
                toApplicationName, toServiceDescList, toAgentFilter);
        return filter.include(linkFilterContext);
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


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LinkFilter{");
        sb.append("fromServiceDescList=").append(fromServiceDescList);
        sb.append(", fromApplicationName='").append(fromApplicationName).append('\'');
        sb.append(", toServiceDescList=").append(toServiceDescList);
        sb.append(", toApplicationName='").append(toApplicationName).append('\'');
        sb.append(", spanResponseConditionFilter=").append(spanResponseConditionFilter);
        sb.append(", spanEventResponseConditionFilter=").append(spanEventResponseConditionFilter);
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


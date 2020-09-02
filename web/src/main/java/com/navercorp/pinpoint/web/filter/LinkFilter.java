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
import com.navercorp.pinpoint.web.filter.transaction.LinkContext;
import com.navercorp.pinpoint.web.filter.transaction.QueueToWasFilter;
import com.navercorp.pinpoint.web.filter.transaction.SpanContext;
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
    private final FilterDescriptor.FromNode fromNode;

    private final List<ServiceType> toServiceDescList;
    private final FilterDescriptor.ToNode toNode;

    private final Filter<SpanBo> spanResponseConditionFilter;

    private final Filter<SpanEventBo> spanEventResponseConditionFilter;

    private final FilterHint filterHint;

    private final AgentFilterFactory agentFilterFactory;
    private final AgentFilter fromAgentFilter;
    private final AgentFilter toAgentFilter;

    private final Filter<LinkContext> filter;

    private final List<RpcHint> rpcHintList;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final URLPatternFilter acceptURLFilter;
    private final URLPatternFilter rpcUrlFilter;

    public LinkFilter(FilterDescriptor filterDescriptor, FilterHint filterHint, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {
        Objects.requireNonNull(filterDescriptor, "filterDescriptor");

        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");

        this.fromNode = filterDescriptor.getFromNode();
        this.fromServiceDescList = findDesc(fromNode.getServiceType(), "fromServiceType");

        this.toNode = filterDescriptor.getToNode();
        this.toServiceDescList = findDesc(toNode.getServiceType(), "toServiceType");

        final ResponseTimeFilter responseTimeFilter = createResponseTimeFilter(filterDescriptor.getResponseTime());
        final ExecutionTypeFilter executionErrorFilter = newExecutionErrorFilter(filterDescriptor.getOption());
        this.spanResponseConditionFilter = newSpanResponseConditionFilter(responseTimeFilter, executionErrorFilter);
        this.spanEventResponseConditionFilter = newSpanEventResponseConditionFilter(responseTimeFilter, executionErrorFilter);

        this.filterHint = Objects.requireNonNull(filterHint, "filterHint");

        final String fromAgentName = fromNode.getAgentId();
        final String toAgentName = toNode.getAgentId();

        this.agentFilterFactory = new AgentFilterFactory(fromAgentName, toAgentName);
        logger.debug("agentFilterFactory:{}", agentFilterFactory);
        this.fromAgentFilter = agentFilterFactory.createFromAgentFilter();
        this.toAgentFilter = agentFilterFactory.createToAgentFilter();

        this.rpcHintList = this.filterHint.getRpcHintList(toNode.getApplicationName());

        // TODO fix : fromSpan base rpccall filter
        this.acceptURLFilter = createAcceptUrlFilter(filterDescriptor.getOption());
        this.rpcUrlFilter = createRpcUrlFilter(filterDescriptor.getOption(), annotationKeyRegistryService);
        logger.info("acceptURLFilter:{}", acceptURLFilter);

        this.filter = resolveLinkFilter();
        logger.info("filter:{}", filter);
    }

    private List<ServiceType> findDesc(String serviceType, String typeName) {
        List<ServiceType> toServiceDescList = serviceTypeRegistryService.findDesc(serviceType);
        if (toServiceDescList == null) {
            throw new IllegalArgumentException(typeName + " not found. toServiceDescList:" + serviceType);
        }
        return toServiceDescList;
    }

    private Filter<SpanBo> newSpanResponseConditionFilter(ResponseTimeFilter responseTimeFilter, ExecutionTypeFilter executionErrorFilter) {
        return new SpanResponseConditionFilter(responseTimeFilter, executionErrorFilter, SpanResponseConditionFilter.ErrorCheck.SPAN);

    }
    private Filter<SpanEventBo> newSpanEventResponseConditionFilter(ResponseTimeFilter responseTimeFilter, ExecutionTypeFilter executionErrorFilter) {
        return new SpanEventResponseConditionFilter(responseTimeFilter, executionErrorFilter);
    }

    private URLPatternFilter createAcceptUrlFilter(FilterDescriptor.Option option) {
        final String urlPattern = option.getUrlPattern();
        if (StringUtils.isEmpty(urlPattern)) {
            return URLPatternFilter::filterAccept;
        }
        // TODO remove decode
        return new AcceptUrlFilter(urlPattern);
    }

    private URLPatternFilter createRpcUrlFilter(FilterDescriptor.Option option, AnnotationKeyRegistryService annotationKeyRegistryService) {
        final String urlPattern = option.getUrlPattern();
        if (StringUtils.isEmpty(urlPattern)) {
            return URLPatternFilter::filterAccept;
        }
        return new RpcURLPatternFilter(urlPattern, serviceTypeRegistryService, annotationKeyRegistryService);
    }

    private ResponseTimeFilter createResponseTimeFilter(FilterDescriptor.ResponseTime responseTime) {
        final ResponseTimeFilterFactory factory = new ResponseTimeFilterFactory(responseTime.getFromResponseTime(), responseTime.getToResponseTime());
        return factory.createFilter();
    }


    private ExecutionTypeFilter newExecutionErrorFilter(FilterDescriptor.Option option) {
        return DefaultExecutionTypeFilter.newExecutionTypeFilter(option.getIncludeException());
    }

    private Filter<LinkContext> resolveLinkFilter() {
        if (includeWas(fromServiceDescList) && includeWas(toServiceDescList)) {
            return new WasToWasFilter(spanResponseConditionFilter, spanEventResponseConditionFilter,
                    acceptURLFilter, agentFilterFactory, rpcUrlFilter, rpcHintList);
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
        SpanContext spanContext = new SpanContext(transaction, serviceTypeRegistryService);

        final String fromApplicationName = fromNode.getApplicationName();
        final String toApplicationName = toNode.getApplicationName();

        LinkContext linkContext = new LinkContext(spanContext,
                fromApplicationName, fromServiceDescList, fromAgentFilter,
                toApplicationName, toServiceDescList, toAgentFilter);
        return filter.include(linkContext);
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
        return "LinkFilter{" +
                "fromServiceDescList=" + fromServiceDescList +
                ", fromNode=" + fromNode +
                ", toServiceDescList=" + toServiceDescList +
                ", toNode=" + toNode +
                ", spanResponseConditionFilter=" + spanResponseConditionFilter +
                ", spanEventResponseConditionFilter=" + spanEventResponseConditionFilter +
                ", filterHint=" + filterHint +
                ", agentFilterFactory=" + agentFilterFactory +
                ", fromAgentFilter=" + fromAgentFilter +
                ", toAgentFilter=" + toAgentFilter +
                ", filter=" + filter +
                ", rpcHintList=" + rpcHintList +
                ", serviceTypeRegistryService=" + serviceTypeRegistryService +
                ", acceptURLFilter=" + acceptURLFilter +
                ", rpcUrlFilter=" + rpcUrlFilter +
                '}';
    }
}


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

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@Component
public class DefaultFilterBuilder implements FilterBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ObjectMapper jsonObjectMapper;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Override
    public Filter build(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            return Filter.NONE;
        }

        try {
            filterText = URLDecoder.decode(filterText, "UTF-8");
            logger.debug("build filter from string. {}", filterText);
        } catch (Exception e) {
            throw new IllegalArgumentException(filterText);
        }
        return makeFilterFromJson(filterText);
    }

    @Override
    public Filter build(String filterText, String filterHint) {
        if (StringUtils.isEmpty(filterText)) {
            return Filter.NONE;
        }

        try {
            filterText = URLDecoder.decode(filterText, "UTF-8");
            logger.debug("build filter from string. {}", filterText);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid filter text. " + filterText);
        }

        if (!StringUtils.isEmpty(filterHint)) {
            try {
                filterHint = URLDecoder.decode(filterHint, "UTF-8");
                logger.debug("build filter hint from string. {}", filterHint);
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid filter hint. " + filterHint);
            }
        } else {
            filterHint = FilterHint.EMPTY_JSON;
        }

        return makeFilterFromJson(filterText, filterHint);
    }

    private Filter makeFilterFromJson(String jsonFilterText) {
        return makeFilterFromJson(jsonFilterText, FilterHint.EMPTY_JSON);
    }

    private Filter makeFilterFromJson(String jsonFilterText, String jsonFilterHint) {
        if (StringUtils.isEmpty(jsonFilterText)) {
            throw new IllegalArgumentException("json string is empty");
        }
        FilterChain chain = new FilterChain();
        try {
            List<FilterDescriptor> list = jsonObjectMapper.readValue(jsonFilterText, new TypeReference<List<FilterDescriptor>>() {
            });

            FilterHint hint = jsonObjectMapper.readValue(jsonFilterHint, new TypeReference<FilterHint>() {
            });

            for (FilterDescriptor descriptor : list) {
                if (!descriptor.isValid()) {
                    throw new IllegalArgumentException("invalid json " + jsonFilterText);
                }

                logger.debug("FilterDescriptor={}", descriptor);

                FromToResponseFilter fromToResponseFilter = createFromToResponseFilter(descriptor, hint);
                chain.addFilter(fromToResponseFilter);

                if (descriptor.isSetUrl()) {
                    FromToFilter fromToFilter = createFromToFilter(descriptor);
                    Filter urlPatternFilter = new URLPatternFilter(fromToFilter, descriptor.getUrlPattern());
                    chain.addFilter(urlPatternFilter);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return chain.get();
    }

    private FromToResponseFilter createFromToResponseFilter(FilterDescriptor descriptor, FilterHint hint) {
        if (descriptor == null) {
            throw new NullPointerException("methodDescriptor must not be null");
        }
        List<ServiceType> fromServiceType = registry.findDesc(descriptor.getFromServiceType());
        if (fromServiceType == null) {
            throw new IllegalArgumentException("fromServiceCode not found. fromServiceType:" + descriptor.getFromServiceType());
        }
        String fromApplicationName = descriptor.getFromApplicationName();
        String fromAgentName = descriptor.getFromAgentName();

        List<ServiceType> toServiceType = registry.findDesc(descriptor.getToServiceType());
        if (toServiceType == null) {
            throw new IllegalArgumentException("toServiceType not found. fromServiceType:" + descriptor.getToServiceType());
        }
        String toApplicationName = descriptor.getToApplicationName();
        String toAgentName = descriptor.getToAgentName();
        Long fromResponseTime = descriptor.getResponseFrom();
        Long toResponseTime = descriptor.getResponseTo();
        Boolean includeFailed = descriptor.getIncludeException();
        return new FromToResponseFilter(fromServiceType, fromApplicationName, fromAgentName, toServiceType, toApplicationName, toAgentName,fromResponseTime, toResponseTime, includeFailed, hint, this.registry);
    }

    private FromToFilter createFromToFilter(FilterDescriptor descriptor) {

        final List<ServiceType> fromServiceTypeList = registry.findDesc(descriptor.getFromServiceType());
        if (fromServiceTypeList == null) {
            throw new IllegalArgumentException("fromServiceCode not found. fromServiceType:" + descriptor.getFromServiceType());
        }
        final List<ServiceType> toServiceTypeList = registry.findDesc(descriptor.getToServiceType());
        if (toServiceTypeList == null) {
            throw new IllegalArgumentException("toServiceTypeList not found. toServiceType:" + descriptor.getToServiceType());
        }

        return new FromToFilter(fromServiceTypeList, descriptor.getFromApplicationName(), toServiceTypeList, descriptor.getToApplicationName());
    }
}

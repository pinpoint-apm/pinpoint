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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectReader;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 *
 * @author netspider
 * @author emeroad
 */
@Component
public class DefaultFilterBuilder implements FilterBuilder<List<SpanBo>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectReader filterHintReader;
    private final ObjectReader filterDescriptorReader;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    public DefaultFilterBuilder(ObjectMapper mapper, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {
        Objects.requireNonNull(mapper, "mapper");
        this.filterHintReader = mapper.readerFor(FilterHint.class);
        this.filterDescriptorReader = mapper.readerFor(new TypeReference<List<FilterDescriptor>>() {});

        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.annotationKeyRegistryService = Objects.requireNonNull(annotationKeyRegistryService, "annotationKeyRegistryService");
    }


    @Override
    public Filter<List<SpanBo>> build(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            return Filter.acceptAllFilter();
        }

        filterText = decode(filterText);
        logger.debug("build filter from string. {}", filterText);

        return makeFilterFromJson(filterText);
    }


    @Override
    public Filter<List<SpanBo>> build(String filterText, String filterHint) {
        if (StringUtils.isEmpty(filterText)) {
            return Filter.acceptAllFilter();
        }

        filterText = decode(filterText);
        logger.debug("build filter from string. {}", filterText);


        if (StringUtils.hasLength(filterHint)) {
            filterHint = decode(filterHint);
        } else {
            filterHint = FilterHint.EMPTY_JSON;
        }
        logger.debug("build filter hint from string. {}", filterHint);

        return makeFilterFromJson(filterText, filterHint);
    }

    private String decode(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF8 decodeFail. value:" + value);
        }
    }

    private Filter<List<SpanBo>> makeFilterFromJson(String jsonFilterText) {
        return makeFilterFromJson(jsonFilterText, FilterHint.EMPTY_JSON);
    }

    private Filter<List<SpanBo>> makeFilterFromJson(String jsonFilterText, String jsonFilterHint) {
        if (StringUtils.isEmpty(jsonFilterText)) {
            throw new IllegalArgumentException("json string is empty");
        }

        final List<FilterDescriptor> filterDescriptorList = readFilterDescriptor(jsonFilterText);
        final FilterHint hint = readFilterHint(jsonFilterHint);
        logger.debug("filterHint:{}", hint);

        List<Filter<List<SpanBo>>> linkFilter = createLinkFilter(jsonFilterText, filterDescriptorList, hint);
        final Filter<List<SpanBo>> filterChain = new FilterChain<>(linkFilter);

        return filterChain;
    }

    private List<Filter<List<SpanBo>>> createLinkFilter(String jsonFilterText, List<FilterDescriptor> filterDescriptorList, FilterHint hint) {
        final List<Filter<List<SpanBo>>> result = new ArrayList<>();
        for (FilterDescriptor descriptor : filterDescriptorList) {
            if (!descriptor.isValid()) {
                throw new IllegalArgumentException("invalid json " + jsonFilterText);
            }

            logger.debug("FilterDescriptor={}", descriptor);
            Filter<List<SpanBo>> filter;
            if (descriptor.getSelfNode().isValid()) {
                filter = new ApplicationFilter(descriptor, serviceTypeRegistryService);
            } else {
                filter = new LinkFilter(descriptor, hint, serviceTypeRegistryService, annotationKeyRegistryService);

            }
            result.add(filter);
        }
        return result;
    }

    private FilterHint readFilterHint(String jsonFilterHint) {
        try {
            return filterHintReader.readValue(jsonFilterHint);
        } catch (IOException e) {
            throw new RuntimeException("FilterHint read fail. error:" + e.getMessage(), e);
        }
    }

    private List<FilterDescriptor> readFilterDescriptor(String jsonFilterText)  {
        try {
            return filterDescriptorReader.readValue(jsonFilterText);
        } catch (IOException e) {
            throw new RuntimeException("FilterDescriptor read fail. error:" + e.getMessage(), e);
        }
    }
}

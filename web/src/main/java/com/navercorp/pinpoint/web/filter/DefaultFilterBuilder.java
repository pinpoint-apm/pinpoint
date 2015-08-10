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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
            final List<FilterDescriptor> list = jsonObjectMapper.readValue(jsonFilterText, new TypeReference<List<FilterDescriptor>>() {});

            final Map<String, List<Object>> hintMap = jsonObjectMapper.readValue(jsonFilterHint, new TypeReference<LinkedHashMap>() {});
            final FilterHint hint = new FilterHint(hintMap);

            for (FilterDescriptor descriptor : list) {
                if (!descriptor.isValid()) {
                    throw new IllegalArgumentException("invalid json " + jsonFilterText);
                }

                logger.debug("FilterDescriptor={}", descriptor);

                chain.addFilter(new FromToResponseFilter(descriptor, hint));

                if (descriptor.isSetUrl()) {
                    chain.addFilter(new URLPatternFilter(descriptor));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return chain.get();
    }
}

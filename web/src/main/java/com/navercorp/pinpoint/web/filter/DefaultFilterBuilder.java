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

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	@Auto    ired
	private ObjectMapper jsonObject    apper;
    	@Override
	public Filter build(String       filterText) {
		if (StringUtils.is          mpty(filterTe                       )) {
			return Filter.NONE;
		}

		try {
			fi          terText = URLDecoder.decode(filterText, "UTF-8");
	       	logger.debug("buil           filter from string. {}", filterText);
		              catch (Exception e) {
			throw         w Illeg    lArgumentException(filterText);
		}
		return makeFilterFr       mJson(filterText);
	}

	@Override
          public Filter                       uild(String filterText, String filterHint) {
	          if (StringUtils.isEmpty(filterText)) {
			return Fi       ter.NONE;
		}

		tr           {
			filterText = URLDecoder.decode(filterText, "UTF-8");
			logge             .debug("build filter from string.                       }", filterText);
		} catch (Exception e) {
	             	throw new IllegalArgumentException("invalid filter te          t. " + filterText             ;
		}

		if (!StringUtils.isEmpty(filterHint)) {
			try {
				fil                          rHint = URLDecoder.decode(filt             rHint, "UTF-8");
				logger.debug("build filt         hint from string. {}", filterHint);
			} catch (Excepti       n e) {
				throw new IllegalArgumentException("invalid filter        int. " + filterHint);
			}
		} else {
			filterHint = FilterHint.EMPTY_JSON; 
	       }

		return makeFilterFromJson(filterT          xt, filterHint);
	}

	private Filter makeFilterFromJs             n(String jsonFilterText) {
		ret       r           makeFilterFromJson(jsonFilterText, FilterHint.EMPTY_JSON);
	}

	private Filter makeFilterFromJson(String jsonFi                   terText, String jsonFilterHint) {
		if (StringUtils.isEmpty(jsonFilterText)) {
			throw                            new IllegalArgumentException(             json string is empty                );
		}
		FilterChain chain = new FilterChain();
		try {
			                         ist<FilterDescriptor> list = jsonObj             ctMapper.readValue(jsonFilterText, new TypeReference<             ist<FilterDescriptor                >() {
			});

			FilterHint hint = jsonO                            jectMapper.          eadValue(jsonFilterHint, new TypeReferen             e<FilterHint>    ) {
			});
			
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

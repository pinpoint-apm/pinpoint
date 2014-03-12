package com.nhn.pinpoint.web.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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

	private Filter makeFilterFromJson(String jsonText) {
		if (StringUtils.isEmpty(jsonText)) {
			throw new IllegalArgumentException("json string is empty");
		}
		FilterChain chain = new FilterChain();
		try {
			List<FilterDescriptor> list = jsonObjectMapper.readValue(jsonText, new TypeReference<List<FilterDescriptor>>() {
			});
			
			for (FilterDescriptor descriptor : list) {
				if (!descriptor.isValid()) {
					throw new IllegalArgumentException("invalid json " + jsonText);
				}

				logger.debug("FilterDescriptor={}", descriptor);
				
				chain.addFilter(new FromToResponseFilter(descriptor));
				
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

package com.nhn.pinpoint.web.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@Component
public class DefaultFilterBuilder implements FilterBuilder {

	private Logger logger = LoggerFactory.getLogger(DefaultFilterBuilder.class);

    @Override
	public Filter build(String filterText) {
		if (StringUtils.isEmpty(filterText)) {
			return Filter.NONE;
		}

		logger.debug("build filter from string. {}", filterText);

		String[] f = filterText.split(Filter.FILTER_DELIMETER);

		Filter filter;
		if (f.length == 1) {
			filter = makeSingleFilter(f[0]);
		} else {
			filter = makeChainedFilter(f);
		}

		// TODO: need cache filter?
		return filter;
	}

	private Filter makeSingleFilter(String filterText) {
		logger.debug("   make filter from string. {}", filterText);
		String[] element = filterText.split(Filter.FILTER_ENTRY_DELIMETER);
		if (element.length == 4) {
			return new FromToFilter(element[0], element[1], element[2], element[3]);
		} else {
			return Filter.NONE;
		}
	}

	private Filter makeChainedFilter(String[] filterTexts) {
		logger.debug("   make chained filter.");
		FilterChain chain = new FilterChain();
		for (String s : filterTexts) {
			chain.addFilter(makeSingleFilter(s));
		}
		return chain;
	}
}

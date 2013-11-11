package com.nhn.pinpoint.web.filter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@Component
public class DefaultFilterBuilder implements FilterBuilder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Pattern FILTER_ENTRY_DELIMETER = Pattern.compile(Filter.FILTER_ENTRY_DELIMETER);
    private static final Pattern FILTER_DELIMETER = Pattern.compile(Filter.FILTER_DELIMETER);

    @Override
	public Filter build(String filterText) {
		if (StringUtils.isEmpty(filterText)) {
			return Filter.NONE;
		}

		logger.debug("build filter from string. {}", filterText);

		final String[] parsedFilterString = FILTER_DELIMETER.split(filterText);

		Filter filter;
		if (parsedFilterString.length == 1) {
			filter = makeSingleFilter(parsedFilterString[0]);
		} else {
			filter = makeChainedFilter(parsedFilterString);
		}

		return filter;
	}

	private Filter makeSingleFilter(String filterText) {
        if (filterText == null) {
            throw new NullPointerException("filterText must not be null");
        }
        logger.debug("   make filter from string. {}", filterText);
		final String[] element = FILTER_ENTRY_DELIMETER.split(filterText);
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

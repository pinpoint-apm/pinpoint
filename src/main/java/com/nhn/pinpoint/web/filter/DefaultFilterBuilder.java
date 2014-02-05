package com.nhn.pinpoint.web.filter;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final Pattern FILTER_ENTRY_DELIMETER = Pattern.compile(Filter.FILTER_ENTRY_DELIMETER);
	private static final Pattern FILTER_DELIMETER = Pattern.compile(Filter.FILTER_DELIMETER);

	// FIXME UI 개발 완료 후 리팩토링.
	private static final String FROM_APPLICATION = "fa";
	private static final String FROM_SERVICE_TYPE = "fst";
	private static final String TO_APPLICATION = "ta";
	private static final String TO_SERVICE_TYPE = "tst";
	private static final String RESPONSE_FROM = "rf";
	private static final String RESPONSE_TO = "rt";
	private static final String INCLUDE_EXCEPTION = "ie";
	private static final String REQUEST_URL_PATTERN = "url";
	
	private final ObjectMapper om = new ObjectMapper();

	@Override
	public Filter build(String filterText) {
		if (StringUtils.isEmpty(filterText)) {
			return Filter.NONE;
		}
		logger.debug("build filter from string. {}", filterText);

		try {
			filterText = URLDecoder.decode(filterText, "UTF-8");
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		
		// FIXME 일단 임시로... UI 개발 완료 후 리팩토링.
		if (filterText.startsWith("[")) {
			return makeFilterFromJson(filterText);
		} else {
			final String[] parsedFilterString = FILTER_DELIMETER.split(filterText);
			if (parsedFilterString.length == 1) {
				return makeSingleFilter(parsedFilterString[0]);
			} else {
				return makeChainedFilter(parsedFilterString);
			}
		}
	}

	// FIXME UI 개발 완료 후 리팩토링.
	private Filter makeFilterFromJson(String jsonText) {
		if (jsonText == null) {
			throw new NullPointerException("jsonText must not be null");
		}

		FilterChain chain = new FilterChain();

		try {
			List<Map<String, Object>> list = om.readValue(jsonText, new TypeReference<List<Map<String, Object>>>() {
			});

			for (Map<String, Object> value : list) {
				String fromApplicationName = value.get(FROM_APPLICATION).toString();
				String fromServiceType = value.get(FROM_SERVICE_TYPE).toString();

				String toApplicationName = value.get(TO_APPLICATION).toString();
				String toServiceType = value.get(TO_SERVICE_TYPE).toString();

				if (StringUtils.isEmpty(fromApplicationName) || StringUtils.isEmpty(fromServiceType) || StringUtils.isEmpty(toApplicationName) || StringUtils.isEmpty(toServiceType)) {
					throw new IllegalArgumentException("invalid json " + jsonText);
				}

				Long fromResponseTime = value.containsKey(RESPONSE_FROM) ? Long.valueOf(value.get(RESPONSE_FROM).toString()) : null;
				
				Long toResponseTime = null;
				if (value.containsKey(RESPONSE_TO)) {
					String v = value.get(RESPONSE_TO).toString();
					if ("max".equals(v)) {
						toResponseTime = Long.MAX_VALUE;
					} else {
						toResponseTime = Long.valueOf(v);
					}
				}

				if ((fromResponseTime == null && toResponseTime != null) || (fromResponseTime != null && toResponseTime == null)) {
					throw new IllegalArgumentException("invalid json " + jsonText);
				}

				Boolean includeFailed = value.containsKey(INCLUDE_EXCEPTION) ? Boolean.valueOf(value.get(INCLUDE_EXCEPTION).toString()) : null;

				chain.addFilter(new FromToResponseFilter(fromServiceType, fromApplicationName, toServiceType, toApplicationName, fromResponseTime, toResponseTime, includeFailed));
				
				if (value.containsKey(REQUEST_URL_PATTERN)) {
					String urlPattern = value.get(REQUEST_URL_PATTERN).toString();
					if (!StringUtils.isEmpty(urlPattern)) {
						chain.addFilter(new URLPatternFilter(fromServiceType, fromApplicationName, toServiceType, toApplicationName, urlPattern));
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		return chain;
	}

	@Deprecated
	private Filter makeSingleFilter(String filterText) {
		if (filterText == null) {
			throw new NullPointerException("filterText must not be null");
		}
		logger.debug("make filter from string. {}", filterText);

		final String[] element = FILTER_ENTRY_DELIMETER.split(filterText);

		// FIXME 이것은 js 수정되기 전에 임시로 사용되는 코드.
		if (element.length == 4) {
			return new FromToFilter(element[0], element[1], element[2], element[3]);
		} else if (element.length == 5) {
			String condition = element[4];
			Boolean includeFailed = null;
			long fromResponseTime;
			long toResponseTime;

			String[] conditions = condition.split(",");
			if (conditions.length == 2) { // from,to
				fromResponseTime = Long.valueOf(conditions[0]);
				toResponseTime = Long.valueOf(conditions[1]);
			} else if (conditions.length == 3) { // error,from,to
				boolean findError = "error".equals(conditions[0]);
				if (!findError) {
					throw new IllegalArgumentException("invalid conditions:" + condition);
				}
				includeFailed = true;
				fromResponseTime = Long.valueOf(conditions[1]);
				toResponseTime = Long.valueOf(conditions[2]);
			} else if (conditions.length == 1) { // error only
				boolean findError = "error".equals(conditions[0]);
				if (!findError) {
					throw new IllegalArgumentException("invalid conditions:" + condition);
				}
				includeFailed = true;
				fromResponseTime = 0;
				toResponseTime = Long.MAX_VALUE;
			} else {
				throw new IllegalArgumentException("invalid conditions:" + condition);
			}
			return new FromToResponseFilter(element[0], element[1], element[2], element[3], fromResponseTime, toResponseTime, includeFailed);
		}
		throw new IllegalArgumentException("Invalid filterText:" + filterText);
	}

	@Deprecated
	private Filter makeChainedFilter(String[] filterTexts) {
		if (logger.isDebugEnabled()) {
			logger.debug("make chained filter. {}", Arrays.toString(filterTexts));
		}
		FilterChain chain = new FilterChain();
		for (String s : filterTexts) {
			chain.addFilter(makeSingleFilter(s));
		}
		return chain;
	}
}

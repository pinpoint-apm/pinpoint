package com.nhn.pinpoint.bootstrap.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class ExcludeUrlFilter implements Filter<String> {

	private final List<String> excludeUrlList;

	public ExcludeUrlFilter(String excludeFormat) {
		this(excludeFormat, ",");
	}

	public ExcludeUrlFilter(String excludeFormat, String separator) {
		if (isEmpty(excludeFormat)) {
			this.excludeUrlList = Collections.emptyList();
			return;
		}
		final String[] split = excludeFormat.split(separator);
		final List<String> arrayList = new ArrayList<String>();
		for (String value : split) {
			if (isEmpty(value)) {
				continue;
			}
			value = value.trim();
			if (value.isEmpty()) {
				continue;
			}
			arrayList.add(value);
		}

		this.excludeUrlList = Collections.unmodifiableList(arrayList);
	}

	private boolean isEmpty(String string) {
		return string == null || string.isEmpty();
	}

	@Override
	public boolean filter(String requestURI) {
		for (String excludeUrl : this.excludeUrlList) {
			if (excludeUrl.equals(requestURI)) {
				return FILTERED;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ExcludeUrlFilter{");
		sb.append("excludeUrlList=").append(excludeUrlList);
		sb.append('}');
		return sb.toString();
	}
}


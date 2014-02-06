package com.nhn.pinpoint.web.filter;

import java.util.List;

import org.springframework.util.AntPathMatcher;

import com.nhn.pinpoint.common.bo.SpanBo;

/**
 * URL filter, URL은 사용자가 요청한 URL, backend를 호출할 때 URL이 있다.
 * 
 * @author netspider
 * 
 */
public class URLPatternFilter implements Filter {

	private final String urlPattern;
	private final Filter fromToFilter;
	private final AntPathMatcher matcher = new AntPathMatcher();

	public URLPatternFilter(FilterDescriptor filterDescriptor) {
		this(filterDescriptor.getFromServiceType(), filterDescriptor.getFromApplicationName(), filterDescriptor.getToServiceType(), filterDescriptor.getToApplicationName(), filterDescriptor.getUrlPattern());
	}
	
	public URLPatternFilter(String fromServiceType, String fromApplicationName, String toServiceType, String toApplicationName, String urlPattern) {
		this.fromToFilter = new FromToFilter(fromServiceType, fromApplicationName, toServiceType, toApplicationName);
		this.urlPattern = urlPattern;
	}

	@Override
	public boolean include(List<SpanBo> transaction) {
		if (fromToFilter.include(transaction)) {
			for (SpanBo span : transaction) {
				if (span.isRoot() && match(span.getRpc())) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean match(String url) {
		return matcher.match(urlPattern, url);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("urlfilter=").append(urlPattern);
		return sb.toString();
	}
}
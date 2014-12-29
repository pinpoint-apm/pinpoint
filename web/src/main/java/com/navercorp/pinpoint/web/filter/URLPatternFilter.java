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

import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.AntPathMatcher;

import com.navercorp.pinpoint.common.bo.SpanBo;

/**
 * There are two kinds of URL. ( URL requested by user, URL requesting a backend server)
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
		this.urlPattern = new String(Base64.decodeBase64(urlPattern), Charset.forName("UTF-8"));
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
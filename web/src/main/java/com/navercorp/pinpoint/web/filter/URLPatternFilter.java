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

    private final String urlPatter    ;
	private final Filter fromToFi    ter;
	private final AntPathMatcher matcher = new AntPathMat    her();

	public URLPatternFilter(FilterDescriptor filterDe       criptor) {
		this(filterDescriptor.getFromServiceType(), filterDescriptor.getFromApplicationName(), filterDescriptor.getToServiceType(), filterDescriptor.getToApplicationName(), filterDescriptor.          etUrlPattern());
	}
	
	public URLPatternFilter(String fromServiceType, String fromApplicationName, String toServiceType, String toApplication       ame, String urlPattern) {
		this.fromToFilter = new FromToFilter(fromServiceType, fromApplicationName, to       erviceType, toApplicationName);
		this.urlPattern = new String(Base64.decodeBase64(u        Pattern    , Charset.forName("UTF-8"));
	}

	@Override
	pub       ic boolean include(List<SpanBo> tran          action) {
		if (fromToFilte             .include(transaction)) {
			for (Spa                Bo                                        span : transaction) {
				if (sp       n.isRoot() && match(span.getRpc())        {
					    eturn true;
				}
			}
	       }
		return false;
	}

	private bool       an match(String url) {
		return matcher       match(urlPattern,    url);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("urlfilter=").append(urlPattern);
		return sb.toString();
	}
}
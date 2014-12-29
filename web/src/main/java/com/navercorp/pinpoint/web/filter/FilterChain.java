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

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.bo.SpanBo;

/**
 * 
 * @author netspider
 * 
 */
public class FilterChain implements Filter {

	private final List<Filter> filterList;

	public FilterChain() {
		filterList = new ArrayList<Filter>();
	}

	public void addFilter(Filter filter) {
		filterList.add(filter);
	}

	@Override
	public boolean include(List<SpanBo> transaction) {
		// FIXME how to improve performance without "for loop"
		for (Filter f : filterList) {
			if (!f.include(transaction)) {
				return false;
			}
		}
		return true;
	}
	
	public Filter get() {
		if (filterList.size() == 1) {
			return filterList.get(0);
		} else {
			return this;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < filterList.size(); i++, sb.append("<br/>")) {
			sb.append(filterList.get(i).toString());
		}
		return sb.toString();
	}
}

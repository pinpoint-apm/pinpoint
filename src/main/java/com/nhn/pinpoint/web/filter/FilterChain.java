package com.nhn.pinpoint.web.filter;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.common.bo.SpanBo;

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

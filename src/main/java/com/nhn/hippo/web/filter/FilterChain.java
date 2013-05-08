package com.nhn.hippo.web.filter;

import java.util.ArrayList;
import java.util.List;

import com.profiler.common.bo.SpanBo;

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
	public boolean exclude(List<SpanBo> span) {
		for (Filter f : filterList) {
			if (!f.exclude(span)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean include(List<SpanBo> span) {
		for (Filter f : filterList) {
			if (!f.include(span)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "FilterChain [filterList=" + filterList + "]";
	}
}

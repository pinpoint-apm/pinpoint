package com.nhn.hippo.web.filter;

import com.profiler.common.bo.SpanBo;

/**
 * 
 * @author netspider
 * 
 */
public interface Filter {

	public static final Filter NONE = new Filter() {
		@Override
		public boolean exclude(SpanBo o) {
			return false;
		}

		@Override
		public boolean include(SpanBo o) {
			return true;
		}
	};

	public static final String FILTER_DELIMETER = "\\^";
	public static final String FILTER_ENTRY_DELIMETER = "\\|";

	// TODO need generic ??
	boolean exclude(SpanBo o);

	boolean include(SpanBo o);
}

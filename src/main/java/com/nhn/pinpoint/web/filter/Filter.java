package com.nhn.pinpoint.web.filter;

import java.util.List;

import com.nhn.pinpoint.common.bo.SpanBo;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public interface Filter {

	public static final Filter NONE = new Filter() {
		@Override
		public boolean include(List<SpanBo> transaction) {
			return true;
		}
	};

	public static final String FILTER_DELIMETER = "\\^";
	public static final String FILTER_ENTRY_DELIMETER = "\\|";

	boolean include(List<SpanBo> transaction);
}

package com.nhn.pinpoint.bootstrap.context;

import com.nhn.pinpoint.common.util.DelegateEnumeration;
import com.nhn.pinpoint.common.util.EmptyEnumeration;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public enum Header {

	HTTP_TRACE_ID("Pinpoint-TraceID"),
	HTTP_SPAN_ID("Pinpoint-SpanID"),
	HTTP_PARENT_SPAN_ID("Pinpoint-pSpanID"),
	HTTP_SAMPLED("Pinpoint-Sampled"),
	HTTP_FLAGS("Pinpoint-Flags"),
	HTTP_PARENT_APPLICATION_NAME("Pinpoint-pAppName"),
	HTTP_PARENT_APPLICATION_TYPE("Pinpoint-pAppType");

	private String name;

	Header(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

	private static final Map<String, Header> NAME_SET = createMap();

	private static Map<String, Header> createMap() {
		Header[] headerList = values();
		Map<String, Header> map = new HashMap<String, Header>();
		for (Header header : headerList) {
			map.put(header.name, header);
		}
		return map;
	}

	public static Header getHeader(String name) {
		if (name == null) {
			return null;
		}
		if (!startWithPinpointHeader(name)) {
			return null;
		}
		return NAME_SET.get(name);
	}



	public static boolean hasHeader(String name) {
		return getHeader(name) != null;
	}

	public static Enumeration getHeaders(String name) {
		if (name == null) {
			return null;
		}
		final Header header = getHeader(name);
		if (header == null) {
			return null;
		}
		// if pinpoint header
		return new EmptyEnumeration();
	}

	public static Enumeration filteredHeaderNames(final Enumeration enumeration) {
		return new DelegateEnumeration(enumeration, FILTER);
	}

	private static DelegateEnumeration.Filter FILTER = new DelegateEnumeration.Filter() {
		@Override
		public boolean filter(Object o) {
			if (o instanceof String) {
				return hasHeader((String )o);
			}
			return false;
		}
	};

	private static boolean startWithPinpointHeader(String name) {
		return name.startsWith("Pinpoint-");
	}
}

package com.nhn.hippo.web.calltree.server;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

/**
 * 
 * @author netspider
 * 
 */
public enum NodeIdGenerator {

	BY_SERVER_INSTANCE, BY_APPLICATION_NAME;

	public String makeServerId(SpanBo span) {
		if (this == BY_SERVER_INSTANCE) {
			return span.getEndPoint();
		} else if (this == BY_APPLICATION_NAME) {
			return span.getServiceName();
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String makeServerId(SubSpanBo span) {
		if (this == BY_SERVER_INSTANCE) {
			return span.getEndPoint();
		} else if (this == BY_APPLICATION_NAME) {
			return span.getServiceName();
		} else {
			throw new IllegalArgumentException();
		}
	}
}

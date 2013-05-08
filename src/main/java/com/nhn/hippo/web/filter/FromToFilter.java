package com.nhn.hippo.web.filter;

import java.util.List;

import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;

/**
 * 
 * @author netspider
 * 
 */
public class FromToFilter implements Filter {

	final short fromServiceCode;
	final String fromApplicationName;
	final short toServiceCode;
	final String toApplicationName;

	public FromToFilter(String fromServiceType, String fromApplicationName, String toServiceType, String toApplicationName) {
		this.fromServiceCode = ServiceType.parse(fromServiceType).getCode();
		this.fromApplicationName = fromApplicationName;
		this.toServiceCode = ServiceType.parse(toServiceType).getCode();
		this.toApplicationName = toApplicationName;
	}

	@Override
	public boolean exclude(List<SpanBo> span) {
		return false;
	}

	@Override
	public boolean include(List<SpanBo> span) {
		return true;
	}

	@Override
	public String toString() {
		return "FromToFilter [fromServiceCode=" + fromServiceCode + ", fromApplicationName=" + fromApplicationName + ", toServiceCode=" + toServiceCode + ", toApplicationName=" + toApplicationName + "]";
	}
}

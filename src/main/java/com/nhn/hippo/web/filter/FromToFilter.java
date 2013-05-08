package com.nhn.hippo.web.filter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEventBo;

/**
 * 
 * @author netspider
 * 
 */
public class FromToFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

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
	public boolean exclude(List<SpanBo> transaction) {
		return false;
	}

	@Override
	public boolean include(List<SpanBo> transaction) {
		boolean include = false;

		for (SpanBo span : transaction) {
			// from이 같으면...
			if (fromServiceCode == span.getServiceType().getCode() && fromApplicationName.equals(span.getApplicationId())) {
				// event 확인.
				List<SpanEventBo> eventBoList = span.getSpanEventBoList();
				for (SpanEventBo event : eventBoList) {
					if (toServiceCode == event.getServiceType().getCode() && toApplicationName.equals(event.getDestinationId())) {
						include = true;
						break;
					}
				}
				if (include) {
					break;
				}
			}
		}

		logger.debug("filter result = {}", include);
		
		return include;
	}

	@Override
	public String toString() {
		return "FromToFilter [fromServiceCode=" + fromServiceCode + ", fromApplicationName=" + fromApplicationName + ", toServiceCode=" + toServiceCode + ", toApplicationName=" + toApplicationName + "]";
	}
}

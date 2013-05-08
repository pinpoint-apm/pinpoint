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

		if (ServiceType.findServiceType(fromServiceCode) == ServiceType.CLIENT || ServiceType.findServiceType(fromServiceCode) == ServiceType.USER) {
			for (SpanBo span : transaction) {
				if (toServiceCode == span.getServiceType().getCode() && toApplicationName.equals(span.getApplicationId())) {
					include = true;
					break;
				}
			}
		} else if (ServiceType.findServiceType(toServiceCode).isUnknown()) {
			for (SpanBo span : transaction) {
				if (fromServiceCode == span.getServiceType().getCode() && fromApplicationName.equals(span.getApplicationId())) {
					List<SpanEventBo> eventBoList = span.getSpanEventBoList();
					for (SpanEventBo event : eventBoList) {
						// client가 있는지만 확인.
						if (event.getServiceType().isRpcClient() && toApplicationName.equals(event.getDestinationId())) {
							include = true;
							break;
						}
					}
					if (include) {
						break;
					}
				}
			}
		} else if (ServiceType.findServiceType(toServiceCode).isWas()) {
			// destination이 was인 경우 src, dest의 span이 모두 존재하겠지...
			int foundCounter = 0;
			for (SpanBo span : transaction) {
				if (fromServiceCode == span.getServiceType().getCode() && fromApplicationName.equals(span.getApplicationId())) {
					foundCounter++;
				}
				if (toServiceCode == span.getServiceType().getCode() && toApplicationName.equals(span.getApplicationId())) {
					foundCounter++;
				}
				if (foundCounter == 2) {
					break;
				}
			}
			include = foundCounter == 2;
		} else {
			for (SpanBo span : transaction) {
				if (fromServiceCode == span.getServiceType().getCode() && fromApplicationName.equals(span.getApplicationId())) {
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
		}

		logger.debug("filter result = {}", include);

		return include;
	}

	@Override
	public String toString() {
		return "FromToFilter [fromServiceCode=" + fromServiceCode + ", fromApplicationName=" + fromApplicationName + ", toServiceCode=" + toServiceCode + ", toApplicationName=" + toApplicationName + "]";
	}
}

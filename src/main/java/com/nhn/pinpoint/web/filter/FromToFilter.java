package com.nhn.pinpoint.web.filter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 * 
 * @author netspider
 * 
 */
public class FromToFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ServiceType fromServiceCode;
    private final String fromApplicationName;
    private final ServiceType toServiceCode;
    private final String toApplicationName;

	public FromToFilter(String fromServiceType, String fromApplicationName, String toServiceType, String toApplicationName) {
        if (fromApplicationName == null) {
            throw new NullPointerException("fromApplicationName must not be null");
        }
        if (toApplicationName == null) {
            throw new NullPointerException("toApplicationName must not be null");
        }

        this.fromServiceCode = ServiceType.parse(fromServiceType);
		this.fromApplicationName = fromApplicationName;
		this.toServiceCode = ServiceType.parse(toServiceType);
		this.toApplicationName = toApplicationName;
	}

	@Override
	public boolean exclude(List<SpanBo> transaction) {
		return false;
	}

	@Override
	public boolean include(List<SpanBo> transaction) {
		boolean include = false;

		if (fromServiceCode == ServiceType.CLIENT || fromServiceCode == ServiceType.USER) {
			for (SpanBo span : transaction) {
				if (span.isRoot() && toServiceCode == span.getServiceType() && toApplicationName.equals(span.getApplicationId())) {
					include = true;
					break;
				}
			}
		} else if (toServiceCode.isUnknown()) {
			for (SpanBo span : transaction) {
                if (fromServiceCode == span.getServiceType() && fromApplicationName.equals(span.getApplicationId())) {
					List<SpanEventBo> eventBoList = span.getSpanEventBoList();
					if (eventBoList == null) {
						continue;
					}
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
		} else if (toServiceCode.isWas()) {
			/**
			 * destination이 was인 경우 src, dest의 span이 모두 존재하겠지... 그리고 circular
			 * check. find src first. from, to와 같은 span이 두 개 이상 존재할 수 있다. 때문에
			 * spanId == parentSpanId도 확인해야함.
			 */
			for (SpanBo srcSpan : transaction) {
				if (fromServiceCode == srcSpan.getServiceType() && fromApplicationName.equals(srcSpan.getApplicationId())) {
					// find dest of src.
					for (SpanBo destSpan : transaction) {
						if (destSpan.getParentSpanId() != srcSpan.getSpanId()) {
							continue;
						}

						if (toServiceCode == destSpan.getServiceType() && toApplicationName.equals(destSpan.getApplicationId())) {
							include = true;
							break;
						}
					}
					if (include) {
						break;
					}
				}
			}
		} else {
			for (SpanBo span : transaction) {
				if (fromServiceCode == span.getServiceType() && fromApplicationName.equals(span.getApplicationId())) {
					List<SpanEventBo> eventBoList = span.getSpanEventBoList();
					if (eventBoList == null) {
						continue;
					}
					for (SpanEventBo event : eventBoList) {
						if (toServiceCode == event.getServiceType() && toApplicationName.equals(event.getDestinationId())) {
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
		StringBuilder sb = new StringBuilder();
		sb.append(fromApplicationName).append(" (").append(fromServiceCode).append(")");
		sb.append(" --&gt; ");
		sb.append(toApplicationName).append(" (").append(toServiceCode).append(")");
		return sb.toString();
	}
}

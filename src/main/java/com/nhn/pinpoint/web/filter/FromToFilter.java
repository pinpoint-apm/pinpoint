package com.nhn.pinpoint.web.filter;

import java.util.List;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 * 
 * @author netspider
 * 
 */
public class FromToFilter implements Filter {

	private final List<ServiceType> fromServiceCode;
    private final String fromApplicationName;
    private final List<ServiceType> toServiceCode;
    private final String toApplicationName;

	public FromToFilter(String fromServiceType, String fromApplicationName, String toServiceType, String toApplicationName) {
        if (fromApplicationName == null) {
            throw new NullPointerException("fromApplicationName must not be null");
        }
        if (toApplicationName == null) {
            throw new NullPointerException("toApplicationName must not be null");
        }

        this.fromServiceCode = ServiceType.findDesc(fromServiceType);
        if (fromServiceCode == null) {
            throw new IllegalArgumentException("fromServiceCode not found. fromServiceType:" + fromServiceType);
        }

        this.fromApplicationName = fromApplicationName;
		this.toServiceCode = ServiceType.findDesc(toServiceType);
        if (toServiceCode == null) {
            throw new IllegalArgumentException("toServiceCode not found. toServiceCode:" + toServiceType);
        }
		this.toApplicationName = toApplicationName;
	}

	@Override
	public boolean exclude(List<SpanBo> transaction) {
		return false;
	}

	@Override
	public boolean include(List<SpanBo> transaction) {

		if (includeServiceType(fromServiceCode, ServiceType.CLIENT) || includeServiceType(fromServiceCode, ServiceType.USER)) {
			for (SpanBo span : transaction) {
				if (span.isRoot() && includeServiceType(toServiceCode, span.getServiceType()) && toApplicationName.equals(span.getApplicationId())) {
					return true;
				}
			}
		} else if (includeUnknown(toServiceCode)) {
			for (SpanBo span : transaction) {
                if (includeServiceType(fromServiceCode, span.getServiceType()) && fromApplicationName.equals(span.getApplicationId())) {
					List<SpanEventBo> eventBoList = span.getSpanEventBoList();
					if (eventBoList == null) {
						continue;
					}
					for (SpanEventBo event : eventBoList) {
						// client가 있는지만 확인.
						if (event.getServiceType().isRpcClient() && toApplicationName.equals(event.getDestinationId())) {
							return true;
						}
					}
				}
			}
		} else if (includeWas(toServiceCode)) {
			/**
			 * destination이 was인 경우 src, dest의 span이 모두 존재하겠지... 그리고 circular
			 * check. find src first. from, to와 같은 span이 두 개 이상 존재할 수 있다. 때문에
			 * spanId == parentSpanId도 확인해야함.
			 */
			for (SpanBo srcSpan : transaction) {
				if (includeServiceType(fromServiceCode, srcSpan.getServiceType()) && fromApplicationName.equals(srcSpan.getApplicationId())) {
					// find dest of src.
					for (SpanBo destSpan : transaction) {
						if (destSpan.getParentSpanId() != srcSpan.getSpanId()) {
							continue;
						}

						if (includeServiceType(toServiceCode, destSpan.getServiceType()) && toApplicationName.equals(destSpan.getApplicationId())) {
							return true;
						}
					}

				}
			}
		} else {
			for (SpanBo span : transaction) {
				if (includeServiceType(fromServiceCode, span.getServiceType()) && fromApplicationName.equals(span.getApplicationId())) {
					List<SpanEventBo> eventBoList = span.getSpanEventBoList();
					if (eventBoList == null) {
						continue;
					}
					for (SpanEventBo event : eventBoList) {
						if (includeServiceType(toServiceCode, event.getServiceType()) && toApplicationName.equals(event.getDestinationId())) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}


    private boolean includeUnknown(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isUnknown()) {
                return true;
            }
        }
        return false;
    }
    private boolean includeWas(List<ServiceType> serviceTypeList) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isWas()) {
                return true;
            }
        }
        return false;
    }

    private boolean includeServiceType(List<ServiceType> serviceTypeList, ServiceType targetServiceType) {
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType == targetServiceType) {
                return true;
            }
        }
        return false;
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

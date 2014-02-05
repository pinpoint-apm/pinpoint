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
public class FromToResponseFilter implements Filter {

	private final List<ServiceType> fromServiceCode;
	private final String fromApplicationName;
	private final List<ServiceType> toServiceCode;
	private final String toApplicationName;

	private final Long fromResponseTime;
	private final Long toResponseTime;
	private final Boolean includeFailed;

	public FromToResponseFilter(String fromServiceType, String fromApplicationName, String toServiceType, String toApplicationName, Long fromResponseTime, Long toResponseTime, Boolean includeFailed) {
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

		this.fromResponseTime = fromResponseTime;
		this.toResponseTime = toResponseTime;
		this.includeFailed = includeFailed;
	}

	private boolean checkResponseCondition(long elapsed, boolean hasError) {
		boolean result = true;
		if (fromResponseTime != null && toResponseTime != null) {
			result &= (elapsed >= fromResponseTime) && (elapsed <= toResponseTime);
		}
		if (includeFailed != null) {
			if (includeFailed) {
				result &= hasError;
			} else {
				result &= !hasError;
			}
		}
		return result;
	}

	@Override
	public boolean include(List<SpanBo> transaction) {
		if (includeServiceType(fromServiceCode, ServiceType.USER)) {
			for (SpanBo span : transaction) {
				if (span.isRoot() && includeServiceType(toServiceCode, span.getServiceType()) && toApplicationName.equals(span.getApplicationId())) {
					return checkResponseCondition(span.getElapsed(), span.getErrCode() > 0);
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
							return checkResponseCondition(event.getEndElapsed(), event.hasException());
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
							return checkResponseCondition(destSpan.getElapsed(), destSpan.getErrCode() > 0);
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
							return checkResponseCondition(event.getEndElapsed(), event.hasException());
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

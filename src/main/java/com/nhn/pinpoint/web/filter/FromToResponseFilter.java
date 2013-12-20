package com.nhn.pinpoint.web.filter;

import java.util.List;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 * FIXME 데모에 사용하려고 급조한 필터
 * 
 * @author netspider
 * 
 */
public class FromToResponseFilter implements Filter {

	private static final String ERROR = "error";

	private final List<ServiceType> fromServiceCode;
	private final String fromApplicationName;
	private final List<ServiceType> toServiceCode;
	private final String toApplicationName;

	private final long fromResponseTime;
	private final long toResponseTime;
	private final boolean findError;

	public FromToResponseFilter(String fromServiceType, String fromApplicationName, String toServiceType, String toApplicationName, String condition) {
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

		if (condition == null) {
			throw new NullPointerException("compare condition must not be null");
		}

		// FIXME 뭔가 엉성하긴 하지만..
		String[] conditions = condition.split(",");
		if (conditions.length == 2) { // from,to
			findError = false;
			fromResponseTime = Long.valueOf(conditions[0]);
			toResponseTime = Long.valueOf(conditions[1]);
		} else if (conditions.length == 3) { // error,from,to
			findError = ERROR.equals(conditions[0]);
			if (!findError) {
				throw new IllegalArgumentException("invalid conditions:" + condition);
			}
			fromResponseTime = Long.valueOf(conditions[1]);
			toResponseTime = Long.valueOf(conditions[2]);
		} else if (conditions.length == 1) { // error only
			findError = ERROR.equals(conditions[0]);
			if (!findError) {
				throw new IllegalArgumentException("invalid conditions:" + condition);
			}
			fromResponseTime = 0;
			toResponseTime = Long.MAX_VALUE;
		} else {
			throw new IllegalArgumentException("invalid conditions:" + condition);
		}
	}

	@Override
	public boolean exclude(List<SpanBo> transaction) {
		return false;
	}

	private boolean checkResponseCondition(long elapsed) {
		return (elapsed >= fromResponseTime) && (elapsed <= toResponseTime);
	}

	@Override
	public boolean include(List<SpanBo> transaction) {
		if (includeServiceType(fromServiceCode, ServiceType.USER)) {
			for (SpanBo span : transaction) {
				if (span.isRoot() && includeServiceType(toServiceCode, span.getServiceType()) && toApplicationName.equals(span.getApplicationId())) {
					if (findError) {
						// FIXME getErrCode로 확인?? hasException으로 확인?? 어떤게 맞지??
						// 서버 맵 스펙하고 맞춰면 될 듯.
						return span.getErrCode() > 0 && checkResponseCondition(span.getElapsed());
					} else {
						return span.getErrCode() == 0 && checkResponseCondition(span.getElapsed());
					}
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
							if (findError) {
								return event.hasException() && checkResponseCondition(event.getEndElapsed());
							} else {
								return !event.hasException() && checkResponseCondition(event.getEndElapsed());
							}
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
							if (findError) {
								// FIXME getErrCode로 확인?? hasException으로 확인??
								// 어떤게 맞지?? 서버 맵 스펙하고 맞춰면 될 듯.
								return destSpan.getErrCode() > 0 && checkResponseCondition(destSpan.getElapsed());
							} else {
								return destSpan.getErrCode() == 0 && checkResponseCondition(destSpan.getElapsed());
							}
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
							if (findError) {
								return event.hasException() && checkResponseCondition(event.getEndElapsed());
							} else {
								return !event.hasException() && checkResponseCondition(event.getEndElapsed());
							}
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

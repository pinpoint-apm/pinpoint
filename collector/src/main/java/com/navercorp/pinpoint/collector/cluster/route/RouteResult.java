package com.nhn.pinpoint.collector.cluster.route;

import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.util.AssertUtils;

public class RouteResult {

	private final RouteStatus status;
	private final ResponseMessage responseMessage;

	public RouteResult(RouteStatus status) {
		this(status, null);
	}

	public RouteResult(RouteStatus status, ResponseMessage responseMessage) {
		this.status = status;
		this.responseMessage = responseMessage;

		if (RouteStatus.OK == status) {
			AssertUtils.assertNotNull(responseMessage, "ResponseMessage may not be null.");
		}
	}

	public RouteStatus getStatus() {
		return status;
	}

	public ResponseMessage getResponseMessage() {
		return responseMessage;
	}

	
	@Override
	public String toString() {
		return status.toString();
	}
	
}

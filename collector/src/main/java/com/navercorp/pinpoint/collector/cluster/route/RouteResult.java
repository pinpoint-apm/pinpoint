package com.nhn.pinpoint.collector.cluster.route;

import com.nhn.pinpoint.rpc.ResponseMessage;

/**
 * @author koo.taejin <kr14910>
 */
public class RouteResult {

	private final RouteStatus status;
	private final ResponseMessage responseMessage;

	public RouteResult(RouteStatus status) {
		this(status, null);
	}

	public RouteResult(RouteStatus status, ResponseMessage responseMessage) {
		this.status = status;
		this.responseMessage = responseMessage;
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

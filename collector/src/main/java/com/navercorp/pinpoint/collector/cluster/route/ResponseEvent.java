package com.nhn.pinpoint.collector.cluster.route;

import org.jboss.netty.channel.Channel;

import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

public class ResponseEvent extends DefaultRouteEvent {

	private final RouteResult routeResult;

	public ResponseEvent(RouteEvent routeEvent, RouteResult routeResult) {
		this(routeEvent.getDeliveryCommand(), routeEvent.getRequestId(), routeEvent.getSourceChannel(), routeResult);
	}

	public ResponseEvent(TCommandTransfer deliveryCommand, int requestId, Channel sourceChannel, RouteResult routeResult) {
		super(deliveryCommand, requestId, sourceChannel);
		this.routeResult = routeResult;
	}

	public RouteResult getRouteResult() {
		return routeResult;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
        sb.append("{");
        sb.append("routeResult=").append(routeResult);
        sb.append("}");

		return super.toString();
	}


}

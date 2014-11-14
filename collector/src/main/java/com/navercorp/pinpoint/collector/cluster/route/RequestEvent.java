package com.nhn.pinpoint.collector.cluster.route;

import org.apache.thrift.TBase;
import org.jboss.netty.channel.Channel;

import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

public class RequestEvent extends DefaultRouteEvent {

	private final TBase requestObject;

	public RequestEvent(RouteEvent routeEvent, TBase requestObject) {
		this(routeEvent.getDeliveryCommand(), routeEvent.getRequestId(), routeEvent.getSourceChannel(), requestObject);
	}

	public RequestEvent(TCommandTransfer deliveryCommand, int requestId, Channel sourceChannel, TBase requestObject) {
		super(deliveryCommand, requestId, sourceChannel);

		this.requestObject = requestObject;
	}

	public TBase getRequestObject() {
		return requestObject;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
        sb.append("{");
        sb.append("requestObject=").append(requestObject);
        sb.append("}");

		return super.toString();
	}

}

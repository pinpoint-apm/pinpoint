package com.nhn.pinpoint.collector.cluster.route;

import org.jboss.netty.channel.Channel;

import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin <kr14910>
 */
public class ResponseEvent extends DefaultRouteEvent {

    private final int requestId;

	private final RouteResult routeResult;

	public ResponseEvent(RouteEvent routeEvent, int requestId, RouteResult routeResult) {
		this(routeEvent.getDeliveryCommand(), routeEvent.getSourceChannel(), requestId, routeResult);
	}

	public ResponseEvent(TCommandTransfer deliveryCommand, Channel sourceChannel, int requestId, RouteResult routeResult) {
		super(deliveryCommand, sourceChannel);
		
		this.requestId = requestId;
		this.routeResult = routeResult;
	}

    public int getRequestId() {
        return requestId;
    }

    public RouteResult getRouteResult() {
		return routeResult;
	}
	
	@Override
	public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("{sourceChannel=").append(getSourceChannel()).append(",");
        sb.append("applicationName=").append(getDeliveryCommand().getApplicationName()).append(",");
        sb.append("agentId=").append(getDeliveryCommand().getAgentId()).append(",");
        sb.append("startTimeStamp=").append(getDeliveryCommand().getStartTime());
        sb.append("requestId=").append(requestId);
        sb.append("routeResult=").append(routeResult);
        sb.append('}');
        
        return sb.toString();
	}

}

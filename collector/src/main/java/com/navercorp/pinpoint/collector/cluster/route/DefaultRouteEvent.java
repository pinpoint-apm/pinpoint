package com.nhn.pinpoint.collector.cluster.route;

import org.jboss.netty.channel.Channel;

import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

public class DefaultRouteEvent implements RouteEvent {

	private final TCommandTransfer deliveryCommand;
	
	private final int requestId;
	
	private final Channel sourceChannel;
	
	
	public DefaultRouteEvent(TCommandTransfer deliveryCommand, int requestId, Channel sourceChannel) {
		this.deliveryCommand = deliveryCommand;
		
		this.requestId = requestId;
		
		this.sourceChannel = sourceChannel;
	}

	@Override
	public TCommandTransfer getDeliveryCommand() {
		return deliveryCommand;
	}

	@Override
	public int getRequestId() {
		return requestId;
	}

	@Override
	public Channel getSourceChannel() {
		return sourceChannel;
	}

	@Override
	public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("{sourceChannel=").append(sourceChannel).append(",");
        sb.append("requestId=").append(requestId).append(",");
        sb.append("applicationName=").append(deliveryCommand.getApplicationName()).append(",");
        sb.append("agentId=").append(deliveryCommand.getAgentId()).append(",");
        sb.append("startTimeStamp=").append(deliveryCommand.getStartTime());
        sb.append('}');
        return sb.toString();
	}
	
}

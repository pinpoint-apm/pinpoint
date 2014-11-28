package com.nhn.pinpoint.collector.cluster.route;

import org.jboss.netty.channel.Channel;

import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin <kr14910>
 */
public class DefaultRouteEvent implements RouteEvent {

    private final TCommandTransfer deliveryCommand;

    private final Channel sourceChannel;

    public DefaultRouteEvent(TCommandTransfer deliveryCommand, Channel sourceChannel) {
        this.deliveryCommand = deliveryCommand;
        this.sourceChannel = sourceChannel;
    }

    @Override
    public TCommandTransfer getDeliveryCommand() {
        return deliveryCommand;
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
        sb.append("applicationName=").append(deliveryCommand.getApplicationName()).append(",");
        sb.append("agentId=").append(deliveryCommand.getAgentId()).append(",");
        sb.append("startTimeStamp=").append(deliveryCommand.getStartTime());
        sb.append('}');
        return sb.toString();
    }

}

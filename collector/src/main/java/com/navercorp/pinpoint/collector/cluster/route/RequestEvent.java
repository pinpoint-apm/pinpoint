package com.navercorp.pinpoint.collector.cluster.route;

import org.apache.thrift.TBase;
import org.jboss.netty.channel.Channel;

import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin <kr14910>
 */
public class RequestEvent extends DefaultRouteEvent {

    private final int requestId;

    private final TBase requestObject;

    public RequestEvent(RouteEvent routeEvent, int requestId, TBase requestObject) {
        this(routeEvent.getDeliveryCommand(), routeEvent.getSourceChannel(), requestId, requestObject);
    }

    public RequestEvent(TCommandTransfer deliveryCommand, Channel sourceChannel, int requestId, TBase requestObject) {
        super(deliveryCommand, sourceChannel);

        this.requestId = requestId;
        this.requestObject = requestObject;
    }

    public int getRequestId() {
        return requestId;
    }

    public TBase getRequestObject() {
        return requestObject;
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
        sb.append("requestObject=").append(requestObject);
        sb.append('}');
        return sb.toString();
    }

}

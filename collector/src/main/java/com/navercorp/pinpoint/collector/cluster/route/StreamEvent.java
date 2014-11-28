package com.nhn.pinpoint.collector.cluster.route;

import org.apache.thrift.TBase;

import com.nhn.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamEvent extends DefaultRouteEvent {

    private final ServerStreamChannelContext streamChannelContext;
    private final TBase requestObject;
    
    public StreamEvent(RouteEvent routeEvent, ServerStreamChannelContext streamChannelContext, TBase requestObject) {
        this(routeEvent.getDeliveryCommand(), streamChannelContext, requestObject);
    }

    public StreamEvent(TCommandTransfer deliveryCommand, ServerStreamChannelContext streamChannelContext, TBase requestObject) {
        super(deliveryCommand, streamChannelContext.getStreamChannel().getChannel());

        this.streamChannelContext = streamChannelContext;
        this.requestObject = requestObject;
    }

    public ServerStreamChannelContext getStreamChannelContext() {
        return streamChannelContext;
    }
    
    public int getStreamChannelId() {
        return getStreamChannelContext().getStreamId();
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
        sb.append("streamChannelContext=").append(getStreamChannelContext());
        sb.append("streamChannelId=").append(getStreamChannelId());
        sb.append("requestObject=").append(requestObject);
        sb.append('}');
        return sb.toString();
    }

}

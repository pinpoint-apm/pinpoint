package com.nhn.pinpoint.collector.cluster.route;

import com.nhn.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.nhn.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamRouteCloseEvent extends DefaultRouteEvent {

    private final ClientStreamChannelContext producerContext;
    private final ServerStreamChannelContext consumerContext;

    public StreamRouteCloseEvent(TCommandTransfer deliveryCommand, ClientStreamChannelContext producerContext, ServerStreamChannelContext consumerContext) {
        super(deliveryCommand, consumerContext.getStreamChannel().getChannel());

        this.producerContext = producerContext;
        this.consumerContext = consumerContext;
    }

    public int getProducerStreamChannelId() {
        return producerContext.getStreamId();
    }
    
    public int getConsumerStreamChannelId() {
        return consumerContext.getStreamId();
    }

    public ClientStreamChannelContext getProducerContext() {
        return producerContext;
    }

    public ServerStreamChannelContext getConsumerContext() {
        return consumerContext;
    }

}

package com.nhn.pinpoint.collector.cluster;

import javax.annotation.PreDestroy;

import org.apache.thrift.TBase;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.cluster.route.DefaultRouteHandler;
import com.nhn.pinpoint.collector.cluster.route.LoggingFilter;
import com.nhn.pinpoint.collector.cluster.route.RequestEvent;
import com.nhn.pinpoint.collector.cluster.route.RouteResult;
import com.nhn.pinpoint.collector.cluster.route.RouteStatus;
import com.nhn.pinpoint.collector.cluster.route.StreamEvent;
import com.nhn.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.stream.BasicStreamPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.nhn.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.nhn.pinpoint.rpc.stream.ServerStreamChannelMessageListener;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.util.SerializationUtils;

/**
 * @author koo.taejin <kr14910>
 */
public class ClusterPointRouter implements MessageListener, ServerStreamChannelMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClusterPointRepository<TargetClusterPoint> targetClusterPointRepository;

    private final DefaultRouteHandler routeHandler;
    private final StreamRouteHandler streamRouteHandler;

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public ClusterPointRouter() {
        this.targetClusterPointRepository = new ClusterPointRepository<TargetClusterPoint>();

        LoggingFilter loggingFilter = new LoggingFilter();

        this.routeHandler = new DefaultRouteHandler(targetClusterPointRepository);
        this.routeHandler.addRequestFilter(loggingFilter.getRequestFilter());
        this.routeHandler.addResponseFilter(loggingFilter.getResponseFilter());
        
        this.streamRouteHandler = new StreamRouteHandler(targetClusterPointRepository);
        this.streamRouteHandler.addRequestFilter(loggingFilter.getStreamCreateFilter());
        this.streamRouteHandler.addResponseFilter(loggingFilter.getResponseFilter());
    }

    @PreDestroy
    public void stop() {
    }

    @Override
    public void handleSend(SendPacket packet, Channel channel) {
        logger.info("Message received {}. channel:{}, packet:{}.", packet.getClass().getSimpleName(), channel, packet);
    }

    @Override
    public void handleRequest(RequestPacket packet, Channel channel) {
        logger.info("Message received {}. channel:{}, packet:{}.", packet.getClass().getSimpleName(), channel, packet);

        TBase<?, ?> request = deserialize(packet.getPayload());
        if (request == null) {
            handleRouteRequestFail("Protocol decoding fail.", packet, channel);
        } else if (request instanceof TCommandTransfer) {
            handleRouteRequest((TCommandTransfer) request, packet, channel);
        } else {
            handleRouteRequestFail("Unknown error.", packet, channel);
        }
    }
    
    @Override
    public short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
        logger.info("Message received {}. streamChannel:{}, packet:{}.", packet.getClass().getSimpleName(), streamChannelContext, packet);

        TBase<?, ?> request = deserialize(packet.getPayload());
        if (request == null) {
            return StreamCreateFailPacket.PACKET_ERROR;
        } else if (request instanceof TCommandTransfer) {
            return handleStreamRouteCreate((TCommandTransfer) request, packet, streamChannelContext);
        } else {
            return StreamCreateFailPacket.UNKNWON_ERROR;
        }
    }
    
    @Override
    public void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet) {
        logger.info("Message received {}. streamChannel:{}, packet:{}.", packet.getClass().getSimpleName(), streamChannelContext, packet);

        streamRouteHandler.close(streamChannelContext);
    }

    private boolean handleRouteRequest(TCommandTransfer request, RequestPacket requestPacket, Channel channel) {
        byte[] payload = ((TCommandTransfer) request).getPayload();
        TBase command = deserialize(payload);

        RouteResult routeResult = routeHandler.onRoute(new RequestEvent((TCommandTransfer) request, channel, requestPacket.getRequestId(), command));

        if (RouteStatus.OK == routeResult.getStatus()) {
            channel.write(new ResponsePacket(requestPacket.getRequestId(), routeResult.getResponseMessage().getMessage()));
            return true;
        } else {
            TResult result = new TResult(false);
            result.setMessage(routeResult.getStatus().getReasonPhrase());

            channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(result)));
            return false;
        }
    }
    
    private void handleRouteRequestFail(String message, RequestPacket requestPacket, Channel channel) {
        TResult tResult = new TResult(false);
        tResult.setMessage(message);

        channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(tResult)));
    }
    
    private short handleStreamRouteCreate(TCommandTransfer request, StreamCreatePacket packet, ServerStreamChannelContext streamChannelContext) {
        byte[] payload = ((TCommandTransfer) request).getPayload();
        TBase command = deserialize(payload);

        RouteResult routeResult = streamRouteHandler.onRoute(new StreamEvent((TCommandTransfer) request, streamChannelContext, command));
        
        RouteStatus status = routeResult.getStatus();
        switch (status) {
            case OK:
                return BasicStreamPacket.SUCCESS;
            case BAD_REQUEST:
                return StreamCreateFailPacket.PACKET_ERROR;
            case NOT_FOUND:
                return StreamCreateFailPacket.ROUTE_NOT_FOUND;
            case NOT_ACCEPTABLE:
                return StreamCreateFailPacket.ROUTE_PACKET_UNSUPPORT;
            case NOT_ACCEPTABLE_UNKNOWN:
                return StreamCreateFailPacket.ROUTE_CONNECTION_ERROR;
            case NOT_ACCEPTABLE_AGENT_TYPE:
                return StreamCreateFailPacket.ROUTE_TYPE_UNKOWN;
            default:
                return StreamCreateFailPacket.UNKNWON_ERROR;
        }
    }

    public ClusterPointRepository<TargetClusterPoint> getTargetClusterPointRepository() {
        return targetClusterPointRepository;
    }

    private byte[] serialize(TBase result) {
        return SerializationUtils.serialize(result, commandSerializerFactory, null);
    }

    private TBase deserialize(byte[] objectData) {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory, null);
    }

}

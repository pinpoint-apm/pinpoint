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
import com.nhn.pinpoint.collector.cluster.route.RouteHandler;
import com.nhn.pinpoint.collector.cluster.route.RouteResult;
import com.nhn.pinpoint.collector.cluster.route.RouteStatus;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
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
public class ClusterPointRouter implements MessageListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ClusterPointRepository<TargetClusterPoint> targetClusterPointRepository;

	private final RouteHandler routeHandler;
	
	@Autowired
	private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

	@Autowired
	private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

	public ClusterPointRouter() {
		this.targetClusterPointRepository = new ClusterPointRepository<TargetClusterPoint>();
		this.routeHandler = new DefaultRouteHandler(targetClusterPointRepository);

		LoggingFilter loggingFilter = new LoggingFilter();
		this.routeHandler.addRequestFilter(loggingFilter.getRequestFilter());
		this.routeHandler.addResponseFilter(loggingFilter.getResponseFilter());
	}

	@PreDestroy
	public void stop() {
	}

	@Override
	public void handleSend(SendPacket sendPacket, Channel channel) {
		logger.info("Received SendPacket {} {}.", sendPacket, channel);
	}

	@Override
	public void handleRequest(RequestPacket requestPacket, Channel channel) {
		logger.info("Received RequestPacket {} {}.", requestPacket, channel);

		TBase<?, ?> request = deserialize(requestPacket.getPayload());

		if (request == null) {
			TResult tResult = new TResult(false);
			tResult.setMessage("Unexpected decode result.");

			channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(tResult)));
		} else if (request instanceof TCommandTransfer) {
			byte[] payload = ((TCommandTransfer) request).getPayload();
			TBase command = deserialize(payload);
			
			RouteResult routeResult = routeHandler.onRoute(new RequestEvent((TCommandTransfer) request, requestPacket.getRequestId(), channel, command));
			
			if (RouteStatus.OK == routeResult.getStatus()) {
				channel.write(new ResponsePacket(requestPacket.getRequestId(), routeResult.getResponseMessage().getMessage()));
			} else {
				TResult result = new TResult(false);
				result.setMessage(routeResult.getStatus().getReasonPhrase());

				channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(result)));
			}
		} else {
			TResult tResult = new TResult(false);
			tResult.setMessage("Unsupported command(" + request + ") type.");

			channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(tResult)));
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

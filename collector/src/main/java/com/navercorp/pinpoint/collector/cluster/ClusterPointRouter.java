package com.nhn.pinpoint.collector.cluster;

import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.receiver.tcp.AgentPropertiesType;
import com.nhn.pinpoint.collector.util.CollectorUtils;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.util.MapUtils;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.io.TCommandTypeVersion;

/**
 * @author koo.taejin <kr14910>
 */
public class ClusterPointRouter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String serverIdentifier = CollectorUtils.getServerIdentifier();

	private final ProfilerClusterPoint profilerClusterPoint;
	private final WebClusterPoint webClusterPoint;

	@Autowired
	private SerializerFactory commandSerializerFactory;

	@Autowired
	private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

	public ClusterPointRouter() {
		this.profilerClusterPoint = new ProfilerClusterPoint();
		this.webClusterPoint = new WebClusterPoint(serverIdentifier, new WebPointMessageListener());
	}

	@PreDestroy
	public void stop() {
		if (webClusterPoint != null) {
			webClusterPoint.close();
		}
	}

	public ProfilerClusterPoint getProfilerClusterPoint() {
		return profilerClusterPoint;
	}

	public WebClusterPoint getWebClusterPoint() {
		return webClusterPoint;
	}

	private byte[] serialize(TBase result) {
		if (result == null) {
			logger.warn("tBase may not be null.");
			return null;
		}

		try {
			HeaderTBaseSerializer serializer = commandSerializerFactory.createSerializer();
			byte[] payload = serializer.serialize(result);
			return payload;
		} catch (TException e) {
			logger.warn(e.getMessage(), e);
		}

		return null;
	}

	private TBase deserialize(byte[] payload) {
		if (payload == null) {
			logger.warn("Payload may not be null.");
			return null;
		}

		try {
			final HeaderTBaseDeserializer deserializer = commandDeserializerFactory.createDeserializer();
			TBase<?, ?> tBase = deserializer.deserialize(payload);
			return tBase;
		} catch (TException e) {
			logger.warn(e.getMessage(), e);
		}

		return null;
	}

	class WebPointMessageListener implements MessageListener {

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

				String applicationName = ((TCommandTransfer) request).getApplicationName();
				String agentId = ((TCommandTransfer) request).getAgentId();
				byte[] payload = ((TCommandTransfer) request).getPayload();

				TBase command = deserialize(payload);
				
				ChannelContext channelContext = profilerClusterPoint.getChannelContext(applicationName, agentId, -1);
				if (channelContext == null) {
					TResult result = new TResult(false);
					result.setMessage(applicationName + "/" + agentId + " can't find suitable ChannelContext.");
					channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(result)));
					return;
				}
				
				Map<Object, Object> proeprties = channelContext.getChannelProperties();
				String version = MapUtils.getString(proeprties, AgentPropertiesType.VERSION.getName());
				
				TCommandTypeVersion commandVersion = TCommandTypeVersion.getVersion(version);
				if (commandVersion.isSupportCommand(command)) {
					Future<ResponseMessage> future = channelContext.getSocketChannel().sendRequestMessage(payload);
					future.await();
					ResponseMessage responseMessage = future.getResult();

					channel.write(new ResponsePacket(requestPacket.getRequestId(), responseMessage.getMessage()));
				} else {
					TResult result = new TResult(false);
					result.setMessage(applicationName + "/" + agentId + " unsupported command(" + command + ") type.");
					
					channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(result)));
				}
			} else {
				TResult tResult = new TResult(false);
				tResult.setMessage("Unsupported command(" + request + ") type.");

				channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(tResult)));
			}

		}
	}

}

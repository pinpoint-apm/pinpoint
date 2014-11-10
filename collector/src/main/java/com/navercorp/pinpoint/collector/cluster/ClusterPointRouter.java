package com.nhn.pinpoint.collector.cluster;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.thrift.TBase;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.util.CollectorUtils;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;
import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.SerializerFactory;
import com.nhn.pinpoint.thrift.io.TCommandTypeVersion;
import com.nhn.pinpoint.thrift.util.SerializationUtils;

/**
 * @author koo.taejin <kr14910>
 */
public class ClusterPointRouter implements MessageListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ClusterPointRepository<TargetClusterPoint> targetClusterPointRepository;

	@Autowired
	private SerializerFactory commandSerializerFactory;

	@Autowired
	private DeserializerFactory commandDeserializerFactory;

	public ClusterPointRouter() {
		this.targetClusterPointRepository = new ClusterPointRepository<TargetClusterPoint>();
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

			String applicationName = ((TCommandTransfer) request).getApplicationName();
			String agentId = ((TCommandTransfer) request).getAgentId();
			long startTimeStamp = ((TCommandTransfer) request).getStartTime();

			byte[] payload = ((TCommandTransfer) request).getPayload();

			List<TargetClusterPoint> clusterPointList = targetClusterPointRepository.getClusterPointList();
			TargetClusterPoint clusterPoint = findClusterPoint(applicationName, agentId, startTimeStamp, clusterPointList);
			if (clusterPoint == null) {
				TResult result = new TResult(false);
				result.setMessage(applicationName + "/" + agentId + " can't find suitable ChannelContext.");
				channel.write(new ResponsePacket(requestPacket.getRequestId(), serialize(result)));
				return;
			}

			TBase command = deserialize(payload);
			TCommandTypeVersion commandVersion = TCommandTypeVersion.getVersion(clusterPoint.gerVersion());
			if (commandVersion.isSupportCommand(command)) {
				Future<ResponseMessage> future = clusterPoint.request(payload);
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

	
	public ClusterPointRepository<TargetClusterPoint> getTargetClusterPointRepository() {
		return targetClusterPointRepository;
	}
	
	private byte[] serialize(TBase result) {
		return SerializationUtils.serialize(result, commandSerializerFactory, null);
	}
	
	private TBase deserialize(byte[] objectData) {
		return SerializationUtils.deserialize(objectData, commandDeserializerFactory, null);
	}

	
	private TargetClusterPoint findClusterPoint(String applicationName, String agentId, long startTimeStamp, List<TargetClusterPoint> targetClusterPointList) {

		List<TargetClusterPoint> result = new ArrayList<TargetClusterPoint>();
		
		for (TargetClusterPoint targetClusterPoint : targetClusterPointList) {
			if (!targetClusterPoint.getApplicationName().equals(applicationName)) {
				continue;
			}
			
			if (!targetClusterPoint.getAgentId().equals(agentId)) {
				continue;
			}
			
			if (!(targetClusterPoint.getStartTimeStamp() == startTimeStamp)) {
				continue;
			}

			result.add(targetClusterPoint);
		}
		
		if (result.size() == 1) {
			return result.get(0);
		}
		
		if (result.size() > 1) {
    		logger.warn("Ambiguous ClusterPoint {}, {}, {} (Valid Agent list={}).", applicationName, agentId, startTimeStamp, result);
			return null;
		}
		
		return null;
	}

}

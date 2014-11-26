package com.nhn.pinpoint.web.server;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.util.NetUtils;
import com.nhn.pinpoint.rpc.packet.HandshakeResponseCode;
import com.nhn.pinpoint.rpc.packet.HandshakeResponseType;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.SocketChannel;
import com.nhn.pinpoint.web.cluster.ClusterManager;
import com.nhn.pinpoint.web.cluster.zookeeper.ZookeeperClusterManager;
import com.nhn.pinpoint.web.config.WebConfig;

/**
 * @author koo.taejin <kr14910>
 */
public class PinpointSocketManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private final Charset charset = Charset.forName("UTF-8");

	// 로컬 ip
	// @Value("#{pinpointWebProps['web.tcpListenI']}")
	private String representationLocalIp;
	private List<String> localIpList;

	private WebConfig config;

	private final PinpointServerSocket pinpointServerSocket;

	private ClusterManager clusterManager;

	public PinpointSocketManager(WebConfig config) {
		this.config = config;
		this.pinpointServerSocket = new PinpointServerSocket();
	}

	@PostConstruct
	public void start() throws KeeperException, IOException, InterruptedException {
		logger.info("{} enable {}.", this.getClass().getSimpleName(), config.isClusterEnable());
		if (config.isClusterEnable()) {
			this.representationLocalIp = getRepresentationLocalV4Ip();
			this.localIpList = NetUtils.getLocalV4IpList();

			logger.info("Representation_Ip = {}, Ip_List = {}", representationLocalIp, localIpList);
			
			// 옵션으로 지정할수 있게 하면 좋을듯 뛰울껀지 말껀지
			if (representationLocalIp.equals(NetUtils.LOOPBACK_ADDRESS_V4) || localIpList.size() == 0) {
				throw new SocketException("Can't find Local Ip.");
			}
			
			String nodeName = representationLocalIp + ":" + config.getClusterTcpPort();
			if (!NetUtils.validationIpPortV4FormatAddress(nodeName)) {
				throw new SocketException("Unexpected LocalAddress. LocalAddress format must be ip:port (" + nodeName + ").");
			}

			this.pinpointServerSocket.setMessageListener(new PinpointSocketManagerHandler());
			this.pinpointServerSocket.bind(representationLocalIp, config.getClusterTcpPort());

			this.clusterManager = new ZookeeperClusterManager(config.getClusterZookeeperAddress(), config.getClusterZookeeperSessionTimeout(), config.getClusterZookeeperRetryInterval());

			// TODO 여기서 수정이 필요함
			// json list는 표준규칙이 아니기 때문에 ip\r\n으로 저장
			this.clusterManager.registerWebCluster(nodeName, convertIpListToBytes(localIpList, "\r\n"));
		}
	}

	@PreDestroy
	public void stop() {
		if (config.isClusterEnable()) {
			if (clusterManager != null) {
				clusterManager.close();
			}
			
			if (pinpointServerSocket != null) {
				pinpointServerSocket.close();
			}
		}
	}

	public List<ChannelContext> getCollectorChannelContext() {
		return pinpointServerSocket.getDuplexCommunicationChannelContext();
	}
	
	public ChannelContext getCollectorChannelContext(String applicationName, String agentId, long startTimeStamp) {
		List<String> agentNameList = clusterManager.getRegisteredAgentList(applicationName, agentId, startTimeStamp);
		
		// AgentName은 중복되는 경우는 문제가 있는 경우임 
		if (agentNameList.size() == 0) {
			logger.warn("{}/{} Can't find agent.", applicationName, agentId);
			return null;
		} else if (agentNameList.size() > 1) {
			logger.warn("{}/{} find dupplicate agent {}.", applicationName, agentId, agentNameList);
			return null;
		}
		
		String agentName = agentNameList.get(0);
		
		List<ChannelContext> channelContextList = getCollectorChannelContext();
		
		for (ChannelContext channelContext : channelContextList) {
			String id = (String) channelContext.getChannelProperties().get("id");
			if (agentName.startsWith(id)) {
				return channelContext;
			}
		}
		
		return null;
	}
	
	private String getRepresentationLocalV4Ip() {
		String ip = NetUtils.getLocalV4Ip();
		
		if (!ip.equals(NetUtils.LOOPBACK_ADDRESS_V4)) {
			return ip;
		}
		
		// LOOPBACK Addess 다 제거하고 나옴
		List<String> ipList = NetUtils.getLocalV4IpList();
		if (ipList.size() > 0) {
			return ipList.get(0);
		}
		
		return NetUtils.LOOPBACK_ADDRESS_V4;
	}

	private byte[] convertIpListToBytes(List<String> ipList, String delimeter) {
		StringBuilder stringBuilder = new StringBuilder();
		
		Iterator<String> ipIterator = ipList.iterator();
		while (ipIterator.hasNext()) {
			String eachIp = ipIterator.next();
			stringBuilder.append(eachIp);
			
			if (ipIterator.hasNext()) {
				stringBuilder.append(delimeter);
			}
		}
		
		return stringBuilder.toString().getBytes(charset);
	}
	
	private class PinpointSocketManagerHandler implements ServerMessageListener {
		@Override
		public void handleSend(SendPacket sendPacket, SocketChannel channel) {
			logger.warn("Unsupport send received {} {}", sendPacket, channel);
		}

		@Override
		public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
			logger.warn("Unsupport request received {} {}", requestPacket, channel);
		}

		@Override
		public HandshakeResponseCode handleHandshake(Map properties) {
			logger.warn("do handShake {}", properties);
			return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
		}
	}

}

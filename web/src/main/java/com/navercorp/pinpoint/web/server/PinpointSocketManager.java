/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.server;

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

import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.SocketChannel;
import com.navercorp.pinpoint.web.cluster.ClusterManager;
import com.navercorp.pinpoint.web.cluster.zookeeper.ZookeeperClusterManager;
import com.navercorp.pinpoint.web.config.WebConfig;

/**
 * @author koo.taejin
 */
public class PinpointSocketManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName()    ;
	private final Charset charset = Charset.forName("UTF-    ");

	//     ocal ip
	// @Value("#{pinpointWebProps['web.tcpL    stenI']}")
	private String represen    ationLocalIp;
	private List<Stri    g> localIpList;

	privat     WebConfig config;

	private final PinpointServerSocket    pinpointServerSocket;

	private Clust    rManager clusterManager;

	public PinpointSock       tManager(WebConfi        config) {
		this.config = config;
		this.pinpointS        verSocket =     ew PinpointServerSocket();
	}

	@PostConstruct
	public void start() throws Ke       perException, IOException, InterruptedException {
		logger.info("{} enable {}.", thi       .getClass().getSimpleName()           config.isClusterEnable());
		if (config.isClusterEn          ble()) {
			this.representationLocalIp = g          tRepresentationLocalV4Ip();
			this.localIpList = NetUtils.getLocalV4IpList();

		                   logger.info("Representation_Ip = {}, Ip_List = {}", representationLocalIp, localI          List);
			
			// TODO might be better to make it configurable whether to keep the proc             ss alive or to kill
			if (representationL                            calIp.equals(NetUtils.LOOPBACK_ADDRESS_V4) || localIpList.si          e() == 0) {
				throw new SocketException("Can't fin              Local Ip.");
			}
			
			String nodeName = representationLocalIp + ":" + config.getClusterTcpPort();
                   		if (!NetUtils.validationIpPortV4FormatAddress(nodeName)) {
				throw n          w SocketException("Unexpected LocalAddress. LocalAddress format must be ip:po          t (" + nodeName + ").");
			}

			this.pinpointServerSocket.setMessageListener(new PinpointSocketManagerHandler());
			this.pinpointServerSocket.bind(representationLocal          p, config.getClusterTcpPort());

			this.clusterManager = new ZookeeperClusterManager(config.getCluster          ookeeperAddress(), config.getClusterZookeeperSessionTimeout(), config.getClusterZookee             erRetryI    terval());

			//        ODO need modification - sto          ing ip list using \r\n              s delimiter sin                            e json list is not s             pported natively
			t                      is.clusterManager.registerWebCluster(nodeName, conv       rtIpListToBytes(localIpList, "\r\n"));
		}
	}

	@PreDestroy
	pu          lic void stop() {
		if (config.isClusterEnable()) {
			if (clusterManager != null) {
				clusterManager.clos       ();
			}
			
			if (pinpointServerSocket != null) {
				pinpointServerSocket.close();
			}
		}
	}

	publi              List<ChannelContext> getCollectorChannelContext() {
		r       turn pinpointServerSocket.ge          DuplexCommunicationChannelContext();
	}
	
	public ChannelConte          t getC       llectorChannelContext(String appli          ationName, String agentId, long startTimeStamp) {
		List<String> agentNameList = c          usterM                   nager.getRegisteredAgentList(ap             licationName, agentId, startTimeStamp);
		
		// having duplicate             AgentName registered is an exceptional case
		if (a          entNameList.size() == 0) {
			logger.warn("{}/{} couldn't find           gent.", applicationName,              gentId);
			re                                     urn null;
		} else if (agentNameList.size(        > 1) {
			logger.warn("{}/{} fo             nd duplicate agent {}.", applicationName           age                   tId, agentNameList);
			return null;
		}
		
		St       ing agentName = agentNameList.get(0);
		
		Lis       <ChannelContext> cha          nelContextList                     getCollectorChannelContext        ;
		
		for (ChannelContext channelContext : channelContextList) {
			Strin        id = (String) channelContext.getChannelProper             ies().get("id");
			if (agentName.startsW       th(id)) {
				return chann          lContext;
			}
		}
		
		retu          n null;
	}
	
	private S                   ring getRepresen             ationLocalV4Ip() {
		Str                            ng ip = NetUtils.getLocalV4Ip();
		
	          if (!ip.equals(NetUtils.LOOPBACK_ADDRESS_V4)) {
			return ip;
		}
		
		//        ocal        p addresses with all LOOPBACK addresses removed
		List<String> ipL          st = NetUtils.getLocalV4IpList();
		if (ipList.size() > 0) {             			r       turn ipList.get(0);
		}
		
		return NetUtils.LOOPBACK_ADDRESS_V4;
	}

	priv          te byte[] convertIpListToBytes(List<String> ipList, String delimet             r) {       		StringBuilder stringBuilder = new StringBuilder();
		
		          terator<String> ipIterator = ipList.i          erator();
		while (ipIterator.hasNext()) {
			String          eachIp = ipIterator.next();
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

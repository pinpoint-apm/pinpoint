package com.nhn.pinpoint.collector.cluster;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;

/**
 * @author koo.taejin <kr14910>
 */
public class WebClusterPoint implements ClusterPoint {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final PinpointSocketFactory factory;

	// InetSocketAddress List로 전달 하는게 좋을거 같은데 이걸 Key로만들기가 쉽지 않네;

	private final Map<InetSocketAddress, PinpointSocket> clusterRepository = new HashMap<InetSocketAddress, PinpointSocket>();

	public WebClusterPoint(String id) {
		factory = new PinpointSocketFactory();
		factory.setTimeoutMillis(1000 * 5);

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("id", id);
		
		factory.setProperties(properties);
	}

	// Not safe for use by multiple threads.
	public void connectPointIfAbsent(InetSocketAddress address) {
		logger.info("localhost -> {} connect started.", address);
		
		if (clusterRepository.containsKey(address)) {
			logger.info("localhost -> {} already connected.", address);
			return;
		}
		
		PinpointSocket socket = createPinpointSocket(address);
		clusterRepository.put(address, socket);
		
		logger.info("localhost -> {} connect completed.", address);
	}

	// Not safe for use by multiple threads.
	public void disconnectPoint(InetSocketAddress address) {
		logger.info("localhost -> {} disconnect started.", address);

		PinpointSocket socket = clusterRepository.remove(address);
		if (socket != null) {
			socket.close();
			logger.info("localhost -> {} disconnect completed.", address);
		} else {
			logger.info("localhost -> {} already disconnected.", address);
		}
	}

	private PinpointSocket createPinpointSocket(InetSocketAddress address) {
		MessageListener messageListener = new SimpleMessageListener(address);
		
		String host = address.getHostName();
		int port = address.getPort();

		PinpointSocket socket = null;
		for (int i = 0; i < 3; i++) {
			try {
				socket = factory.connect(host, port, messageListener);
				logger.info("tcp connect success:{}/{}", host, port);
				return socket;
			} catch (PinpointSocketException e) {
				logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
			}
		}
		logger.warn("change background tcp connect mode  {}/{} ", host, port);
		socket = factory.scheduledConnect(host, port, messageListener);

		return socket;
	}

	public List<InetSocketAddress> getWebClusterList() {
		return new ArrayList<InetSocketAddress>(clusterRepository.keySet());
	}
	
	public void close() {
		for (PinpointSocket socket : clusterRepository.values()) {
			if (socket != null) {
				socket.close();
			}
		}
		
		if (factory != null) {
			factory.release();
		}
	}

	class SimpleMessageListener implements MessageListener {

		private final InetSocketAddress address;

		public SimpleMessageListener(InetSocketAddress address) {
			this.address = address;
		}

		@Override
		public void handleSend(SendPacket sendPacket, Channel channel) {
			logger.info("{} receive send Message {}", address, sendPacket);
			// TODO Auto-generated method stub

		}

		@Override
		public void handleRequest(RequestPacket requestPacket, Channel channel) {
			logger.info("{} receive Request Message {}", address, requestPacket);
		}

	}

}

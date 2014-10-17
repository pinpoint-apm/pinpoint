package com.nhn.pinpoint.profiler;


import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.thrift.TException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.profiler.receiver.CommandDispatcher;
import com.nhn.pinpoint.profiler.sender.TcpDataSender;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.SocketChannel;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializerFactory;

public class HeartBeatCheckerTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final int PORT = 10050;
	public static final String HOST = "127.0.0.1";

	@Test
	public void checkerTest1() throws InterruptedException {
		AtomicInteger requestCount = new AtomicInteger();
		AtomicInteger successCount = new AtomicInteger();
		
		ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

		PinpointServerSocket server = createServer(serverListener);
		
        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

		TcpDataSender sender = new TcpDataSender(socket);
		HeartBeatChecker checker = new HeartBeatChecker(sender, 1000L, getAgentInfo());

		try {
			checker.start();

			Thread.sleep(10000);

			Assert.assertEquals(1, successCount.get());
		} finally {
			closeAll(server, checker, socket, socketFactory);
		}
	}

	@Test
	public void checkerTest2() throws InterruptedException {
		AtomicInteger requestCount = new AtomicInteger();
		AtomicInteger successCount = new AtomicInteger();

		ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount, Integer.MAX_VALUE);

		PinpointServerSocket server = createServer(serverListener);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender sender = new TcpDataSender(socket);
		HeartBeatChecker checker = new HeartBeatChecker(sender, 1000L, getAgentInfo());

		try {
			checker.start();

			Thread.sleep(10000);

			Assert.assertEquals(2, requestCount.get());
			Assert.assertEquals(0, successCount.get());
		} finally {
			closeAll(server, checker, socket, socketFactory);
		}
	}

	@Test
	public void checkerTest3() throws InterruptedException {
		AtomicInteger requestCount = new AtomicInteger();
		AtomicInteger successCount = new AtomicInteger();

		ResponseServerMessageListener serverListener = new ResponseServerMessageListener(requestCount, successCount);

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

		TcpDataSender sender = new TcpDataSender(socket);
		HeartBeatChecker checker = new HeartBeatChecker(sender, 1000L, getAgentInfo());

		try {
			checker.start();

			createAndDeleteServer(serverListener, 10000L);
			Thread.sleep(1000);
			createAndDeleteServer(serverListener, 10000L);
			Thread.sleep(1000);
			createAndDeleteServer(serverListener, 10000L);
			
			Assert.assertEquals(3, successCount.get());
		} finally {
			closeAll(null, checker, socket, socketFactory);
		}
	}

	private PinpointServerSocket createServer(ServerMessageListener listener) {
		PinpointServerSocket server = new PinpointServerSocket();
		// server.setMessageListener(new
		// NoResponseServerMessageListener(requestCount));
		server.setMessageListener(listener);
		server.bind(HOST, PORT);

		return server;
	}
	
	private void createAndDeleteServer(ServerMessageListener listner, long waitTimeMillis) throws InterruptedException {
		PinpointServerSocket server = null;
		try {
			server = createServer(listner);
			Thread.sleep(waitTimeMillis);
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}

	private void closeAll(PinpointServerSocket server, HeartBeatChecker checker, PinpointSocket socket, PinpointSocketFactory factory) {
		if (server != null) {
			server.close();
		}

		if (checker != null) {
			checker.stop();
		}
		
		if (socket != null) {
			socket.close();
		}
		
		if (factory != null) {
			factory.release();
		}
	}

	private TAgentInfo getAgentInfo() {
		TAgentInfo agentInfo = new TAgentInfo("hostname", "127.0.0.1", "8081", "agentId", "appName", (short) 2, 1111, "1", System.currentTimeMillis());
		return agentInfo;
	}

	class ResponseServerMessageListener implements ServerMessageListener {
		private final AtomicInteger requestCount;
		private final AtomicInteger successCount;

		private final int successCondition;
		
		public ResponseServerMessageListener(AtomicInteger requestCount, AtomicInteger successCount) {
			this(requestCount, successCount, 1);
		}
		
		public ResponseServerMessageListener(AtomicInteger requestCount, AtomicInteger successCount, int successCondition) {
			this.requestCount = requestCount;
			this.successCount = successCount;
			this.successCondition = successCondition;
		}

		@Override
		public void handleSend(SendPacket sendPacket, SocketChannel channel) {
			logger.info("handleSend:{}", sendPacket);

		}

		@Override
		public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
			int requestCount = this.requestCount.incrementAndGet();
			
			if (requestCount < successCondition) {
				return;
			}
			
			logger.info("handleRequest~~~:{}", requestPacket);

			try {
				HeaderTBaseSerializer serializer = HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer();

				TResult result = new TResult(true);
				byte[] resultBytes = serializer.serialize(result);

				this.successCount.incrementAndGet();
				
				channel.sendResponseMessage(requestPacket, resultBytes);
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public int handleEnableWorker(Map arg0) {
			return 0;
		}
	}
	
	private PinpointSocketFactory createPinpointSocketFactory() {
    	PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setTimeoutMillis(1000 * 5);
        pinpointSocketFactory.setProperties(Collections.EMPTY_MAP);
        pinpointSocketFactory.setMessageListener(new CommandDispatcher());

        return pinpointSocketFactory;
	}

    
    private PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory) {
    	PinpointSocket socket = null;
    	for (int i = 0; i < 3; i++) {
            try {
                socket = factory.connect(host, port);
                logger.info("tcp connect success:{}/{}", host, port);
                return socket;
            } catch (PinpointSocketException e) {
            	logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
    	logger.warn("change background tcp connect mode  {}/{} ", host, port);
        socket = factory.scheduledConnect(host, port);
    	
        return socket;
    }

}

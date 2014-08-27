package com.nhn.pinpoint.profiler.sender;

import java.util.Collections;
import java.util.Map;

import com.nhn.pinpoint.thrift.dto.TApiMetaData;
import com.nhn.pinpoint.profiler.receiver.CommandDispatcher;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.StreamPacket;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.ServerStreamChannel;
import com.nhn.pinpoint.rpc.server.SocketChannel;

import net.sf.cglib.proxy.Factory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TcpDataSenderReconnectTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = 10050;
    public static final String HOST = "127.0.0.1";

    private int send;

    public PinpointServerSocket serverStart() {
        PinpointServerSocket server = new PinpointServerSocket();
        server.setMessageListener(new ServerMessageListener() {

            @Override
            public void handleSend(SendPacket sendPacket, SocketChannel channel) {
                logger.info("handleSend:{}", sendPacket);
                send++;
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
                logger.info("handleRequest:{}", requestPacket);
            }

            @Override
            public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
                logger.info("handleStreamPacket:{}", streamPacket);
            }

			@Override
			public int handleEnableWorker(Map properties) {
				return 0;
			}
        });
        server.bind(HOST, PORT);
        return server;
    }


    @Test
    public void connectAndSend() throws InterruptedException {
        PinpointServerSocket old = serverStart();

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender sender = new TcpDataSender(socket);
        Thread.sleep(500);
        old.close();

        Thread.sleep(500);
        logger.info("Server start------------------");
        PinpointServerSocket pinpointServerSocket = serverStart();

        Thread.sleep(5000);
        logger.info("sendMessage------------------");
        sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));

        Thread.sleep(500);
        logger.info("sender stop------------------");
        sender.stop();

        pinpointServerSocket.close();
        socket.close();
        socketFactory.release();
    }
    
    private PinpointSocketFactory createPinpointSocketFactory() {
    	PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setTimeoutMillis(1000 * 5);
        pinpointSocketFactory.setProperties(Collections.EMPTY_MAP);

        return pinpointSocketFactory;
	}

    
    private PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory) {
    	MessageListener messageListener = new CommandDispatcher();
    	
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
}

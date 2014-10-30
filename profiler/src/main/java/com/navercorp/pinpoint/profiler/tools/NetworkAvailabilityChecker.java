package com.nhn.pinpoint.profiler.tools;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.receiver.CommandDispatcher;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.TcpDataSender;
import com.nhn.pinpoint.profiler.sender.UdpDataSender;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;

/**
 * 
 * @author netspider
 * 
 */
public class NetworkAvailabilityChecker implements PinpointTools {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NetworkAvailabilityChecker.class);
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("usage : " + NetworkAvailabilityChecker.class.getSimpleName() + " AGENT_CONFIG_FILE");
			return;
		}

		String configPath = args[0];

        DataSender udpSender = null;
        DataSender udpSpanSender = null;
        DataSender tcpSender = null;
        
        PinpointSocketFactory socketFactory = null;
        PinpointSocket socket = null;
		try {
			ProfilerConfig profilerConfig = new ProfilerConfig();
			profilerConfig.readConfigFile(configPath);

			String collector = profilerConfig.getCollectorServerIp();
			int uPort = profilerConfig.getCollectorUdpServerPort();
			int usPort = profilerConfig.getCollectorUdpSpanServerPort();
			int tPort = profilerConfig.getCollectorTcpServerPort();

			udpSender = new UdpDataSender(collector, uPort, "UDP", 10);
			udpSpanSender = new UdpDataSender(collector, usPort, "UDP-SPAN", 10);
			
			socketFactory = createPinpointSocketFactory();
			socket = createPinpointSocket(collector, tPort, socketFactory);
			
			tcpSender = new TcpDataSender(socket);

			boolean udpSenderResult = udpSender.isNetworkAvailable();
			boolean udpSpanSenderResult = udpSpanSender.isNetworkAvailable();
			boolean tcpSenderResult = tcpSender.isNetworkAvailable();

			StringBuilder sb = new StringBuilder();
			sb.append("\nTEST RESULT\n");
			sb.append("UDP://").append(collector).append(":").append(uPort).append("=").append((udpSenderResult) ? "OK" : "FAILED").append("\n");
			sb.append("UDP://").append(collector).append(":").append(usPort).append("=").append((udpSpanSenderResult) ? "OK" : "FAILED").append("\n");
			sb.append("TCP://").append(collector).append(":").append(tPort).append("=").append((tcpSenderResult) ? "OK" : "FAILED").append("\n");

			System.out.println(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
            closeDataSender(udpSender);
            closeDataSender(udpSpanSender);
            closeDataSender(tcpSender);
			System.out.println("END.");
			
			if (socket != null) {
				socket.close();
			}
			if (socketFactory != null) {
				socketFactory.release();
			}
		}
    }

    private static void closeDataSender(DataSender dataSender) {
        if (dataSender != null) {
            dataSender.stop();
        }
    }
    
    private static PinpointSocketFactory createPinpointSocketFactory() {
    	PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setTimeoutMillis(1000 * 5);
        pinpointSocketFactory.setProperties(Collections.<String, Object>emptyMap());
        pinpointSocketFactory.setMessageListener(new CommandDispatcher.Builder().build());

        return pinpointSocketFactory;
	}

    
    private static PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory) {
    	PinpointSocket socket = null;
    	for (int i = 0; i < 3; i++) {
            try {
                socket = factory.connect(host, port);
                LOGGER.info("tcp connect success:{}/{}", host, port);
                return socket;
            } catch (PinpointSocketException e) {
            	LOGGER.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
    	LOGGER.warn("change background tcp connect mode  {}/{} ", host, port);
        socket = factory.scheduledConnect(host, port);
    	
        return socket;
    }
    
}

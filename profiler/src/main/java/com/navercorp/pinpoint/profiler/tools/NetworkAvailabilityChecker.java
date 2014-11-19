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

        DataSender udpStatSender = null;
        DataSender udpSpanSender = null;
        DataSender tcpSender = null;

        PinpointSocketFactory socketFactory = null;
        PinpointSocket socket = null;
        try {
            ProfilerConfig profilerConfig = new ProfilerConfig();
            profilerConfig.readConfigFile(configPath);

            String collectorStatIp = profilerConfig.getCollectorStatServerIp();
            int collectorStatPort = profilerConfig.getCollectorStatServerPort();
            udpStatSender = new UdpDataSender(collectorStatIp, collectorStatPort, "UDP-STAT", 10);

            String collectorSpanIp = profilerConfig.getCollectorSpanServerIp();
            int collectorSpanPort = profilerConfig.getCollectorSpanServerPort();
            udpSpanSender = new UdpDataSender(collectorSpanIp, collectorSpanPort, "UDP-SPAN", 10);

            String collectorTcpIp = profilerConfig.getCollectorTcpServerIp();
            int collectorTcpPort = profilerConfig.getCollectorTcpServerPort();
            socketFactory = createPinpointSocketFactory();
            socket = createPinpointSocket(collectorTcpIp, collectorTcpPort, socketFactory);

            tcpSender = new TcpDataSender(socket);

            boolean udpSenderResult = udpStatSender.isNetworkAvailable();
            boolean udpSpanSenderResult = udpSpanSender.isNetworkAvailable();
            boolean tcpSenderResult = tcpSender.isNetworkAvailable();

            StringBuilder buffer = new StringBuilder();
            buffer.append("\nTEST RESULT\n");
            write(buffer, "UDP-STAT://", collectorStatIp, collectorStatPort, udpSenderResult);
            write(buffer, "UDP-SPAN://", collectorSpanIp, collectorSpanPort, udpSpanSenderResult);
            write(buffer, "TCP://", collectorTcpIp, collectorTcpPort, tcpSenderResult);

            System.out.println(buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDataSender(udpStatSender);
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

	private static void write(StringBuilder buffer, String protcol, String collectorStatIp, int collectorStatPort, boolean udpSenderResult) {
        buffer.append(protcol);
        buffer.append(collectorStatIp);
        buffer.append(":");
        buffer.append(collectorStatPort);
        buffer.append("=");
        buffer.append((udpSenderResult) ? "OK" : "FAILED");
        buffer.append("\n");
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

        RuntimeException lastException = null;
        for (int i = 0; i < 3; i++) {
            try {
                PinpointSocket socket = factory.connect(host, port);
                LOGGER.info("tcp connect success:{}/{}", host, port);
                return socket;
            } catch (PinpointSocketException e) {
                LOGGER.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
                lastException = e;
            }
        }
        throw lastException;
    }
    
}

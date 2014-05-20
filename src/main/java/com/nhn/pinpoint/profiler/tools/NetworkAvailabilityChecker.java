package com.nhn.pinpoint.profiler.tools;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.TcpDataSender;
import com.nhn.pinpoint.profiler.sender.UdpDataSender;

/**
 * 
 * @author netspider
 * 
 */
public class NetworkAvailabilityChecker implements PinpointTools {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("usage : " + NetworkAvailabilityChecker.class.getSimpleName() + " AGENT_CONFIG_FILE");
			return;
		}

		String configPath = args[0];

        DataSender udpSender = null;
        DataSender udpSpanSender = null;
        DataSender tcpSender = null;
		try {
			ProfilerConfig profilerConfig = new ProfilerConfig();
			profilerConfig.readConfigFile(configPath);

			String collector = profilerConfig.getCollectorServerIp();
			int uPort = profilerConfig.getCollectorUdpServerPort();
			int usPort = profilerConfig.getCollectorUdpSpanServerPort();
			int tPort = profilerConfig.getCollectorTcpServerPort();

			udpSender = new UdpDataSender(collector, uPort, "UDP", 10);
			udpSpanSender = new UdpDataSender(collector, usPort, "UDP-SPAN", 10);
			tcpSender = new TcpDataSender(collector, tPort);

			boolean udpSenderResult = udpSender.isNetworkAvalable();
			boolean udpSpanSenderResult = udpSpanSender.isNetworkAvalable();
			boolean tcpSenderResult = tcpSender.isNetworkAvalable();

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
		}
    }

    private static void closeDataSender(DataSender dataSender) {
        if (dataSender != null) {
            dataSender.stop();
        }
    }
}

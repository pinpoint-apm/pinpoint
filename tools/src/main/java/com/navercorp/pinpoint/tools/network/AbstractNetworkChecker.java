package com.navercorp.pinpoint.tools.network;

import com.navercorp.pinpoint.tools.utils.HostResolver;
import com.navercorp.pinpoint.tools.utils.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public abstract class AbstractNetworkChecker implements NetworkChecker {

    private static final String WHITE_SPACE = "    "; // 4space
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final Logger logger = new Logger();

    private final String testName;

    private final InetSocketAddress hostAddress;
    private final List<InetSocketAddress> ipAddressList;

    public AbstractNetworkChecker(String testName, InetSocketAddress hostAddress) throws UnknownHostException {
        this.testName = testName;
        this.hostAddress = hostAddress;
        this.ipAddressList = HostResolver.getIPList(hostAddress);
    }

    protected abstract boolean check(InetSocketAddress address) throws IOException;

    protected abstract boolean check(InetSocketAddress address, byte[] requestData, byte[] expectedResponseData) throws IOException;

    @Override
    public void check() throws IOException {
        StringBuilder report = new StringBuilder(32);

        String hostName = getHostName(hostAddress);
        report.append(testName).append(":// ").append(hostName).append(LINE_SEPARATOR);

        for (InetSocketAddress ipAddress : ipAddressList) {
            boolean check = check(ipAddress);
            report.append(createReport(ipAddress, check));
        }

        logger.info(report);
    }

    @Override
    public void check(byte[] requestData, byte[] expectedResponseData) throws IOException {
        StringBuilder report = new StringBuilder(32);

        String hostName = getHostName(hostAddress);
        report.append(testName).append(":// ").append(hostName).append(LINE_SEPARATOR);

        for (InetSocketAddress ipAddress : ipAddressList) {
            boolean check = check(ipAddress, requestData, expectedResponseData);
            report.append(createReport(ipAddress, check));
        }

        logger.info(report);
    }

    private String getHostName(InetSocketAddress hostAddress) {
        return hostAddress.getHostName();
    }

    private String createReport(InetSocketAddress socketAddress, boolean check) {
        String ip = getIp(socketAddress, socketAddress.getHostName());
        int port = socketAddress.getPort();

        String report = WHITE_SPACE + "=> " + ip + ":" + port +
                " [" + (check ? "SUCCESS" : "FAIL") + "]" + LINE_SEPARATOR;
        return report;
    }

    protected String getIp(InetSocketAddress socketAddress, String defaultValue) {
        InetAddress address = socketAddress.getAddress();
        if (address != null) {
            return address.getHostAddress();
        }
        return defaultValue;
    }

}

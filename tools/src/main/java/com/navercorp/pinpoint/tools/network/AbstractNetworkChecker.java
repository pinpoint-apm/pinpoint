package com.navercorp.pinpoint.tools.network;

import com.navercorp.pinpoint.tools.utils.HostResolver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public abstract  class AbstractNetworkChecker implements NetworkChecker {

    private static final String WHITE_SPACE = "    "; // 4space
    private static final String LINE_SEPARATOR = "\r\n";

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

        System.out.println(report.toString());

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

        System.out.println(report.toString());
    }

    private String getHostName(InetSocketAddress hostAddress) {
        String hostName = hostAddress.getHostName();
        return hostName;
    }

    private String createReport(InetSocketAddress socketAddress, boolean check) {
        String ip = getIp(socketAddress, socketAddress.getHostName());
        int port = socketAddress.getPort();

        StringBuilder report = new StringBuilder();
        report.append(WHITE_SPACE).append("=> ").append(ip).append(":").append(port);
        report.append(" [").append(check ? "SUCCESS" : "FAIL").append("]").append(LINE_SEPARATOR);
        return report.toString();
    }

    protected String getIp(InetSocketAddress socketAddress, String defaultValue) {
        InetAddress address = socketAddress.getAddress();
        if (address != null) {
            return address.getHostAddress();
        }
        return defaultValue;
    }

}

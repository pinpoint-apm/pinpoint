package com.navercorp.pinpoint.tools.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class UDPChecker extends AbstractNetworkChecker {

    public UDPChecker(String testName, String hostName, int port) throws UnknownHostException {
        this(testName, new InetSocketAddress(hostName, port));
    }

    public UDPChecker(String testName, InetSocketAddress hostAddress) throws UnknownHostException {
        super(testName, hostAddress);
    }

    @Override
    protected boolean check(InetSocketAddress address) throws IOException {
        DatagramSocket socket = null;
        try {
            socket = createSocket(address);

            return socket.isConnected();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        return false;
    }

    @Override
    protected boolean check(InetSocketAddress address, byte[] requestData, byte[] expectedResponseData) {
        DatagramSocket socket = null;
        try {
            socket = createSocket();

            write(socket, requestData, address);
            byte[] responseData = read(socket, expectedResponseData.length);

            return Arrays.equals(expectedResponseData, responseData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        return false;
    }

    private DatagramSocket createSocket() throws IOException {
        DatagramSocket socket = new DatagramSocket();

        socket.setSoTimeout(3000);
        return socket;
    }

    private DatagramSocket createSocket(InetSocketAddress socketAddress) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.connect(socketAddress);

        socket.setSoTimeout(3000);
        return socket;
    }

    private void write(DatagramSocket socket, byte[] requestData, InetSocketAddress address) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(requestData, requestData.length, address);
        socket.send(datagramPacket);
    }

    private byte[] read(DatagramSocket socket, int readSize) throws IOException {
        byte[] buf = new byte[readSize];

        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        socket.receive(datagramPacket);

        return buf;
    }

}

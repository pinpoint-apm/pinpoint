package com.navercorp.pinpoint.tools.network;

import com.navercorp.pinpoint.tools.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class TCPChecker extends AbstractNetworkChecker {

    public TCPChecker(String testName, String hostName, int port) throws UnknownHostException {
        this(testName, new InetSocketAddress(hostName, port));
    }

    public TCPChecker(String testName, InetSocketAddress hostAddress) throws UnknownHostException {
        super(testName, hostAddress);
    }


    @Override
    protected boolean check(InetSocketAddress address) throws IOException {
        Socket socket = null;
        try {
            socket = createSocket(address);
            return socket.isConnected();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(socket);
        }
        return false;
    }

    @Override
    protected boolean check(InetSocketAddress address, byte[] requestData, byte[] expectedResponseData) throws IOException {
        Socket socket = null;
        try {
            socket = createSocket(address);

            write(socket, requestData);
            byte[] responseData = read(socket, 100);

            return Arrays.equals(expectedResponseData, responseData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(socket);
        }
        return false;
    }

    private Socket createSocket(InetSocketAddress socketAddress) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.setSoTimeout(3000);
            socket.connect(socketAddress);
        } catch (IOException ioe){
            IOUtils.closeQuietly(socket);
            throw ioe;
        }
        return socket;
    }

    private void write(Socket socket, byte[] requestData) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(requestData);
        outputStream.flush();
    }

    private byte[] read(Socket socket, int readSize) throws IOException {
        byte[] buf = new byte[readSize];

        InputStream inputStream = socket.getInputStream();
        // TODO readFully()
        int read = inputStream.read(buf);
        if (read == IOUtils.EOF) {
            return IOUtils.EMPTY_BYTES;
        }

        return Arrays.copyOf(buf, read);
    }

}

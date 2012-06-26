package com.profiler.sender;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.profiler.config.TomcatProfilerReceiverConfig;

public class TCPInfoRequestSender {
	public TCPInfoRequestSender() {
		
	}
	public void requestAgentInfo() {
//		InetSocketAddress socketAddress=(InetSocketAddress)packet.getSocketAddress();
		try {
//			String address=socketAddress.getHostName();
//			InetAddress inetAddress=socketAddress.getAddress();
//			int port=packet.getPort();
//			Socket socket=new Socket(inetAddress, port);
//			socket.
//			System.out.println(address);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

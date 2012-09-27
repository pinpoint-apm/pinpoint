package com.profiler.receiver;

import com.profiler.config.ProfilerConfig;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPSocketManager extends Thread {
	ServerSocket serverSocket = null;

	public TCPSocketManager() {
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(ProfilerConfig.AGENT_TCP_LISTEN_PORT, 100);
			System.out.println("*** Start TomcatProfiler TCP Listen Thread ***");
			while (true) {
				Socket socket = serverSocket.accept();
				InputStream stream = socket.getInputStream();
				byte[] readData = new byte[1024];
				stream.read(readData);
				System.out.println(new String(readData));
			}
			// } catch(InterruptedException ie) {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

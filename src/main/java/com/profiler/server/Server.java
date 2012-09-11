package com.profiler.server;

import com.profiler.server.receiver.tcp.TCPReceiver;
import com.profiler.server.receiver.udp.DataReceiver;
import com.profiler.server.receiver.udp.MulplexedUDPReceiver;

public class Server {
	public static void main(String[] args) {
		// Log4jConfigurer.configure("log4j.xml");
		new Server().start();
	}

	public void start() {
		System.out.println("Start MulplexedUDPReceiver Receive UDP Thread");
		DataReceiver mulplexDataReceiver = new MulplexedUDPReceiver();
		mulplexDataReceiver.start();

		System.out.println("Start Tomcat Agent Data Receive TDP Thread");
		TCPReceiver tcpReceiver = new TCPReceiver();
		tcpReceiver.start();

		// System.out.println("***** Start Fetch data Thread                    ********");
		// FetchTPSDataThread fetchRPS = new FetchTPSDataThread();
		// fetchRPS.start();
		// System.out.println("*********************************************************");
	}
}

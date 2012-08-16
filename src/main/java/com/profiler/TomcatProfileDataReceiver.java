package com.profiler;

import com.profiler.receiver.udp.DataReceiver;
import com.profiler.receiver.udp.MulplexedUDPReceiver;
import org.apache.log4j.PropertyConfigurator;

import com.profiler.data.thread.FetchTPSDataThread;
import com.profiler.receiver.tcp.TCPReceiver;

public class TomcatProfileDataReceiver {
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		TomcatProfileDataReceiver receiver = new TomcatProfileDataReceiver();
		receiver.collect();
	}

	public void collect() {
		// System.out.println("***** Start Thrift server *****");
		// ThriftReceiver thrift=new ThriftReceiver();
		// thrift.start();

		System.out.println("******************************************************");
		System.out.println("***** Start TomcatTransactionData Receive UDP Thread *****"); // request
																							// start
																							// stop
		DataReceiver mulplexDataReceiver = new MulplexedUDPReceiver();

		System.out.println("***** Start Tomcat Agent Data Receive TDP Thread *****"); // was
																						// start
																						// stop
		TCPReceiver tcpReceiver = new TCPReceiver();

		System.out.println("***** Start Fetch data Thread                    *****");
		FetchTPSDataThread fetchRPS = new FetchTPSDataThread();

		System.out.println("******************************************************");

		mulplexDataReceiver.start();
		tcpReceiver.start();
		fetchRPS.start();
	}
}

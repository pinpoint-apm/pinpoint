package com.profiler.server;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.profiler.server.receiver.tcp.TCPReceiver;
import com.profiler.server.receiver.udp.DataReceiver;
import com.profiler.server.receiver.udp.MultiplexedUDPReceiver;

public class Server {
	public static void main(String[] args) {
		new Server().start();
	}

	private DataReceiver mulplexDataReceiver;
	private TCPReceiver tcpReceiver;
	GenericApplicationContext context;

	public void start() {

		context = createContext();
		System.out.println("Start MultiplexedUDPReceiver Receive UDP Thread");

		mulplexDataReceiver = new MultiplexedUDPReceiver(context);
		mulplexDataReceiver.start();

		System.out.println("Start Tomcat Agent Data Receive TDP Thread");
		tcpReceiver = new TCPReceiver();
		tcpReceiver.start();

		addShutdownHook();
		// System.out.println("***** Start Fetch data Thread                    ********");
		// FetchTPSDataThread fetchRPS = new FetchTPSDataThread();
		// fetchRPS.start();
		// System.out.println("*********************************************************");
	}

	private GenericApplicationContext createContext() {
		GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		ClassPathResource resource = new ClassPathResource("applicationContext.xml");
		context.load(resource);

		context.refresh();
		// 순서 보장을 할수가 없음.
		// context.registerShutdownHook();
		return context;
	}

	private void shutdown() {
		if (mulplexDataReceiver != null) {
			System.out.println("Shutdown MultiplexedUDPReceiver Receive UDP Thread");
			mulplexDataReceiver.shutdown();
			System.out.println("Shutdown MultiplexedUDPReceiver complete");
		}
		if (context != null) {
			context.close();
		}

	}

	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				shutdown();
			}
		}));
	}
}

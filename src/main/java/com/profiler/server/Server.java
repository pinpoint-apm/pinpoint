package com.profiler.server;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.profiler.server.receiver.udp.DataReceiver;
import com.profiler.server.receiver.udp.MultiplexedUDPReceiver;

public class Server {

	private static final Logger logger = LoggerFactory.getLogger("com.profiler.server.Server");

	public static void main(String[] args) {
		new Server().start();
	}

	private DataReceiver mulplexDataReceiver;
	GenericApplicationContext context;

	public void start() {
		logger.info("Initializing server components.");
		context = createContext();

		boolean successfullyStarted = true;

		logger.info("Starting MultiplexedUDPReceiver receive UDP Thread.");
		mulplexDataReceiver = new MultiplexedUDPReceiver(context);
		Future<Boolean> startFuture = mulplexDataReceiver.start();

		try {
			successfullyStarted &= startFuture.get();
		} catch (Exception e) {
			startFuture.cancel(true);
			logger.error("Failed to start multiplexDataReceiver.");
		}

		if (successfullyStarted) {
			logger.info("Server started successfully.");
		} else {
			logger.warn("Server started incompletely.");
		}

		addShutdownHook();
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
			logger.info("Shutdown MultiplexedUDPReceiver Receive UDP Thread.");
			mulplexDataReceiver.shutdown();
			logger.info("Shutdown MultiplexedUDPReceiver complete.");
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

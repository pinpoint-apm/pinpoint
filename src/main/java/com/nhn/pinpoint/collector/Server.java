package com.nhn.pinpoint.collector;

import java.util.concurrent.Future;

import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.collector.receiver.tcp.TCPReceiver;
import com.nhn.pinpoint.collector.receiver.udp.DataReceiver;
import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import com.nhn.pinpoint.collector.receiver.udp.UDPReceiver;
import com.nhn.pinpoint.collector.spring.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class Server {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public static void main(String[] args) {
		new Server().start();
	}

	private DataReceiver udpDataReceiver;
    private TCPReceiver tcpReceiver;
	private GenericApplicationContext context;

	public void start() {
		logger.info("Initializing server components.");
        try {
            context = createContext();
            DispatchHandler dispatchHandler = ApplicationContextUtils.getDispatchHandler(context);


            CollectorConfiguration configuration = new CollectorConfiguration();
            configuration.readConfigFile();

            tcpServerStart(configuration, dispatchHandler);
            udpServerStart(configuration, dispatchHandler);
        } catch (Exception ex) {
            logger.error("pinpoint collector start fail. Caused:{}", ex.getMessage(), ex);
            shutdown();
            return;
        }

        addShutdownHook();
	}

    private void udpServerStart(CollectorConfiguration configuration, DispatchHandler dispatchHandler) {
        logger.info("Starting UDPReceiver.");
        // 여기서 Exception이 날수 있음.
        this.udpDataReceiver = crateUdpReceiver(configuration, dispatchHandler);
        // 여기서 Exception이 날수 있음.
        this.udpDataReceiver.start();

        logger.info("Server started successfully.");
    }

    private UDPReceiver crateUdpReceiver(CollectorConfiguration configuration, DispatchHandler dispatchHandler) {

        // 옵션 설정.
        UDPReceiver udpDataReceiver = new UDPReceiver(dispatchHandler, configuration.getCollectorUdpListenPort());
        udpDataReceiver.setWorkerThreadSize(configuration.getUdpWorkerThread());
        udpDataReceiver.setWorkerQueueSize(configuration.getUdpWorkerQueueSize());
        return udpDataReceiver;
    }

    private void tcpServerStart(CollectorConfiguration configuration, DispatchHandler dispatchHandler) {
        logger.info("Starting TCPReceiver.");
        tcpReceiver = new TCPReceiver(dispatchHandler, configuration.getCollectorTcpListenPort());
        tcpReceiver.start();
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
        if(tcpReceiver != null) {
            logger.info("Shutdown TCPReceiver.");
            tcpReceiver.stop();
            logger.info("Shutdown TCPReceiver complete.");
        }

		if (udpDataReceiver != null) {
			logger.info("Shutdown UDPReceiver.");
			udpDataReceiver.shutdown();
			logger.info("Shutdown UDPReceiver complete.");
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

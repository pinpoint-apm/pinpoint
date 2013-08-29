package com.nhn.pinpoint.collector;

import com.nhn.pinpoint.collector.config.CollectorConfiguration;
import com.nhn.pinpoint.collector.receiver.tcp.TCPReceiver;
import com.nhn.pinpoint.collector.receiver.DataReceiver;
import com.nhn.pinpoint.collector.receiver.DispatchHandler;
import com.nhn.pinpoint.collector.receiver.udp.UDPReceiver;
import com.nhn.pinpoint.collector.spring.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class Server {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public static void main(String[] args) {
		new Server().start();
	}

	private DataReceiver udpDataReceiver;
    private TCPReceiver tcpReceiver;
	private GenericApplicationContext context;
	private StatServer statServer;

	public void start() {
		logger.info("Initializing server components.");
        try {
            context = createContext();
            CollectorConfiguration configuration = context.getBean("collectorConfiguration", CollectorConfiguration.class);
            DispatchHandler dispatchHandler = ApplicationContextUtils.getDispatchHandler(context);
            tcpServerStart(configuration, dispatchHandler);
            udpServerStart(context);
            statServerStart(configuration);
        } catch (Exception ex) {
            logger.error("pinpoint collector start fail. Caused:{}", ex.getMessage(), ex);
            shutdown();
            return;
        }

        addShutdownHook();
	}

	private void statServerStart(CollectorConfiguration configuration) {
		statServer = context.getBean("statServer", StatServer.class);
		statServer.setPort(configuration.getCollectorStatListenPort());
		statServer.start();
	}
	
    private void udpServerStart(GenericApplicationContext context) {
        logger.info("Starting UDPReceiver.");
        // 여기서 Exception이 날수 있음.
        this.udpDataReceiver = context.getBean("udpReceiver", UDPReceiver.class);
        // 여기서 Exception이 날수 있음.
        this.udpDataReceiver.start();

        logger.info("Server started successfully.");
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
//      context close하면 닫김.
//		if (udpDataReceiver != null) {
//			logger.info("Shutdown UDPReceiver.");
//			udpDataReceiver.shutdown();
//			logger.info("Shutdown UDPReceiver complete.");
//		}
		
		if (statServer != null) {
			logger.info("Sutdown StatServer.");
			statServer.shutdown();
			logger.info("Shutdown StatServer complete.");
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

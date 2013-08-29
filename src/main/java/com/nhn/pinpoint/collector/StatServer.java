package com.nhn.pinpoint.collector;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.nhn.pinpoint.collector.monitor.AgentStatStore;
import com.nhn.pinpoint.collector.monitor.servlet.AgentStatServlet;

/**
 * 통계 정보를 제공하는 HTTP 서버.
 * 
 * pinpoint-collector.properties의 collectorStatListenPort로 포트를 지정할 수 있다.
 * (default=9996)
 * 
 * @author harebox
 * 
 */
public class StatServer implements InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Logger reporterLogger = LoggerFactory.getLogger("com.nhn.pinpoint.collector.StateReport");
	
    @Autowired
    private MetricsServlet metricsServlet;
    
    @Autowired
    private AgentStatServlet agentStatServlet;
    
    @Autowired
    private MetricRegistry metricRegistry;
    
    @Autowired
    private AgentStatStore agentStatStore;
    
    private ScheduledReporter reporter;
	
	private Server server;
	private ServletContextHandler contextHandler;
	private int port = 9996;
	
	public void afterPropertiesSet() throws Exception {
		initRegistry();
		initReporters();
	}
	
	public void start() {
		try {
			initServlets(port);
			
			server.start();
			logger.info("StatServer started");
			server.join();
		} catch (Exception e) {
			logger.error("StatServer error : {}", e.toString());
		}
	}
	
	public void shutdown() {
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("StatServer error : {}", e.toString());
		}
        shutdownReporter();
	}

    public MetricRegistry getRegistry() {
        return metricRegistry;
    }
    
    public AgentStatStore getStore() {
    	return agentStatStore;
    }

    void initRegistry() {
		// add JVM statistics
    	metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
    	metricRegistry.register("jvm.vm", new JvmAttributeGaugeSet());
    	metricRegistry.register("jvm.garbage-collectors", new GarbageCollectorMetricSet());
    	metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet());
	}
	
	void initServlets(int port) {
		contextHandler = new ServletContextHandler();
		contextHandler.setContextPath("/");
		
		// metrics servlet
		ServletHolder metricsServletHolder = new ServletHolder(metricsServlet);
		contextHandler.addServlet(metricsServletHolder, "/stats");
		
		// store servlet
		ServletHolder storeServletHolder = new ServletHolder(agentStatServlet);
		contextHandler.addServlet(storeServletHolder, "/agents");
		
		server = new Server(port);
		server.setHandler(contextHandler);
	}
	
	void initReporters() {
        Slf4jReporter.Builder builder = Slf4jReporter.forRegistry(metricRegistry);
        builder.convertRatesTo(TimeUnit.SECONDS);
        builder.convertDurationsTo(TimeUnit.MILLISECONDS);

        builder.outputTo(reporterLogger);
        reporter = builder.build();

		reporter.start(60, TimeUnit.SECONDS); // print every 1 min.
	}

    public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private void shutdownReporter() {
        if (reporter == null) {
            return;
        }
        reporter.stop();
    }
	
}

package com.nhn.pinpoint.collector;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlets.MetricsServlet;

/**
 * 통계 정보를 제공하는 HTTP 서버.
 * 
 * pinpoint-collector.properties의 collectorStatListenPort로 포트를 지정할 수 있다.
 * (default=9996)
 * 
 * @author harebox
 * 
 */
public class StatServer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Logger reporterLogger = LoggerFactory.getLogger("com.nhn.pinpoint.collector.StateReport");
	
	private final MetricRegistry registry = new MetricRegistry();

    private ScheduledReporter reporter;
	
	private Server server;
	private ServletContextHandler contextHandler;
	
	public StatServer(int port) {
		initRegistry();
		initServlets(port);
		initReporters();
	}
	
	public void start() {
		try {
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
        return registry;
    }

    void initRegistry() {
		// add JVM statistics
		registry.register("jvm.memory", new MemoryUsageGaugeSet());
		registry.register("jvm.vm", new JvmAttributeGaugeSet());
		registry.register("jvm.garbage-collectors", new GarbageCollectorMetricSet());
		registry.register("jvm.thread-states", new ThreadStatesGaugeSet());
	}
	
	void initServlets(int port) {
		MetricsServlet metricsServlet = new MetricsServlet(registry);
		ServletHolder servletHolder = new ServletHolder(metricsServlet);

		contextHandler = new ServletContextHandler();
		contextHandler.setContextPath("/");
		contextHandler.addServlet(servletHolder, "/stats");
		
		server = new Server(port);
		server.setHandler(contextHandler);
	}
	
	void initReporters() {
        Slf4jReporter.Builder builder = Slf4jReporter.forRegistry(registry);
        builder.convertRatesTo(TimeUnit.SECONDS);
        builder.convertDurationsTo(TimeUnit.MILLISECONDS);

        builder.outputTo(reporterLogger);
        reporter = builder.build();

		reporter.start(60, TimeUnit.SECONDS); // print every 1 min.

	}

    private void shutdownReporter() {
        if (reporter == null) {
            return;
        }
        reporter.stop();
    }


	
}

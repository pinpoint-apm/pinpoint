package com.nhn.pinpoint.profiler.monitor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.GarbageCollector;
import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 * AgentStat monitor
 * 
 * @author harebox
 * 
 */
public class AgentStatMonitor {

    private static final long DEFAULT_INTERVAL = 1000 * 5;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new PinpointThreadFactory("Pinpoint-stat-monitor", true));

	private final DataSender dataSender;
	private final String agentId;

	public AgentStatMonitor(DataSender dataSender, String agentId) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.dataSender = dataSender;
        this.agentId = agentId;
	}


	public void start() {
		CollectJob job = new CollectJob(dataSender);
		// FIXME 설정에서 수집 주기를 가져올 수 있어야 한다.
		long interval = DEFAULT_INTERVAL;
		long wait = 0;
		
		executor.scheduleAtFixedRate(job, wait, interval, TimeUnit.MILLISECONDS);
		logger.info("AgentStat monitor started");
	}

	public void stop() {
		executor.shutdown();
        try {
            executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("AgentStat monitor stopped");
	}

    class CollectJob implements Runnable {
		private TAgentStat agentStat;
		private DataSender dataSender;
		private MetricMonitorRegistry monitorRegistry;
		private GarbageCollector garbageCollector;

		public CollectJob(DataSender dataSender) {
			this.dataSender = dataSender;

			// FIXME 디폴트 레지스트리를 생성하여 사용한다. 다른데서 쓸 일이 있으면 외부에서 삽입하도록 하자.
			this.monitorRegistry = new MetricMonitorRegistry();

			// FIXME 설정에 따라 어떤 데이터를 수집할 지 선택할 수 있도록 해야한다. 여기서는 JVM 메모리 정보를 default로 수집.
			this.monitorRegistry.registerJvmMemoryMonitor(new MonitorName(MetricMonitorValues.JVM_MEMORY));
			this.monitorRegistry.registerJvmGcMonitor(new MonitorName(MetricMonitorValues.JVM_GC));

			// TAgentStat 객체를 준비한다.
			this.agentStat = new TAgentStat();
			this.agentStat.setAgentId(agentId);

			// GarbageCollector 타입을 확인한다.
			this.garbageCollector = new GarbageCollector();
			this.garbageCollector.setType(monitorRegistry);
			if (logger.isInfoEnabled()) {
				logger.info("found : {}", this.garbageCollector);
			}
		}
		
		public void run() {
			try {
				agentStat.setTimestamp(System.currentTimeMillis());
				garbageCollector.map(monitorRegistry, agentStat, agentId);
				dataSender.send(agentStat);
			} catch (Exception ex) {
				logger.warn("AgentStat collect failed. Caused:{}", ex.getMessage(), ex);
			}
		}
	}

}

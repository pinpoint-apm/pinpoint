package com.nhn.pinpoint.profiler.monitor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.nhn.pinpoint.ProductInfo;
import com.nhn.pinpoint.thrift.dto.AgentInfo;
import com.nhn.pinpoint.thrift.dto.AgentStat;
import com.nhn.pinpoint.thrift.dto.StatWithCmsCollector;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorMapper;
import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.sender.DataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AgentStat monitor
 * 
 * @author harebox
 * 
 */
public class AgentStatMonitor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final long DEFAULT_INTERVAL = 5;
	
	private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(5, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread t = new Thread(runnable);
			t.setName(ProductInfo.CAMEL_NAME + "-stat-monitor");
			return t;
		}
	});

	private DataSender dataSender;
	private AgentInfo agentInfo;
	private TraceContext traceContext;
	private final ProfilerConfig profilerConfig;

	public AgentStatMonitor(TraceContext traceContext, ProfilerConfig profilerConfig) {
		if (traceContext == null) {
			throw new NullPointerException("traceContext is null");
		}
		this.traceContext = traceContext;
		this.profilerConfig = profilerConfig;
	}

	public void setDataSender(DataSender dataSender) {
		this.dataSender = dataSender;
	}
	
	public void setAgentInfo(AgentInfo agentInfo) {
		this.agentInfo = agentInfo;
	}

	public void start() {
		CollectJob job = new CollectJob(dataSender, traceContext, profilerConfig);
		// FIXME 설정에서 수집 주기를 가져올 수 있어야 한다.
		long interval = DEFAULT_INTERVAL;
		long wait = 0;
		
		executor.scheduleAtFixedRate(job, wait, interval, TimeUnit.SECONDS);
		logger.info("AgentStat monitor started");
	}

	public void shutdown() {
		executor.shutdown();
		logger.info("AgentStat monitor stopped");
	}

	class CollectJob implements Runnable {
		private AgentStat agentStat;
		private DataSender dataSender;
		private TraceContext traceContext;
		private ProfilerConfig profilerConfig;
		private MetricMonitorRegistry monitorRegistry;
		private MetricMonitorMapper monitorMapper;

		public CollectJob(DataSender dataSender, TraceContext traceContext, ProfilerConfig profilerConfig) {
			this.dataSender = dataSender;
			this.traceContext = traceContext;
			this.profilerConfig = profilerConfig;
			
			// FIXME 디폴트 레지스트리를 생성하여 사용한다. 다른데서 쓸 일이 있으면 외부에서 삽입하도록 하자.
			this.monitorRegistry = new MetricMonitorRegistry();
			
			// FIXME 설정에 따라 어떤 데이터를 수집할 지 선택할 수 있도록 해야한다. 여기서는 JVM 메모리 정보를 default로 수집.
			this.monitorRegistry.registerJvmMemoryMonitor(new MonitorName("jvm", "memory"));
			this.monitorRegistry.registerJvmGcMonitor(new MonitorName("jvm", "gc"));
			
			this.monitorMapper = new MetricMonitorMapper();
		}
		
		public void run() {
			// AgentStat 객체를 준비한다.
			if (agentInfo != null && this.agentStat == null) {
				if (this.monitorRegistry.getRegistry().getNames().contains("jvm.gc.ConcurrentMarkSweep.count")) {
					logger.info("found : CMS collector");
					StatWithCmsCollector cms = new StatWithCmsCollector();
					cms.setAgentId(agentInfo.getAgentId());
					this.agentStat = new AgentStat();
					this.agentStat.setCms(cms);
				}
			}
			if (this.agentStat == null) {
				logger.info("AgentStat is not available");
				return;
			}
			try {
				// 통계 Registry에 있는 데이터를 메시지 객체에 매핑한다.
				this.monitorMapper.map(this.monitorRegistry, this.agentStat);

				// send queue에 삽입한다.
				dataSender.send(agentStat);
			} catch (Exception e) {
				logger.warn("AgentStat collect failed : {}", e.getMessage());
			}
		}
	}

}

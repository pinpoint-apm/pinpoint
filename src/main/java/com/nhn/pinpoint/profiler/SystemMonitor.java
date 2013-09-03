package com.nhn.pinpoint.profiler;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import com.nhn.pinpoint.ProductInfo;
import com.nhn.pinpoint.thrift.dto.JVMInfoThriftDTO;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.util.Assert;
import com.sun.management.OperatingSystemMXBean;

/**
 * System monitor
 * 
 * @author netspider
 */
@SuppressWarnings("restriction")
public class SystemMonitor {

	private static final Logger logger = LoggerFactory.getLogger(SystemMonitor.class.getName());

	private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread t = new Thread(runnable);
			t.setName(ProductInfo.CAMEL_NAME + "-SystemMonitor");
			t.setDaemon(true);
			return t;
		}
	});

	private DataSender dataSender;
	private TraceContext traceContext;
	private final ProfilerConfig profilerConfig;

	public SystemMonitor(TraceContext traceContext, ProfilerConfig profilerConfig) {
		Assert.notNull(traceContext, "traceContext must not be null");
		this.traceContext = traceContext;
		this.profilerConfig = profilerConfig;
	}

	public void setDataSender(DataSender dataSender) {
		this.dataSender = dataSender;
	}

	public void start() {
//		logger.info("Starting system monitor.");
//		executor.scheduleAtFixedRate(new Worker(dataSender, traceContext, profilerConfig), 5, 5, TimeUnit.SECONDS);
	}

	public void stop() {
//		logger.info("Stopping system monitor");
//		executor.shutdown();
	}

	private static class Worker implements Runnable {

		private DataSender dataSender;
		private TraceContext traceContext;
		private ProfilerConfig profilerConfig;

		public Worker(DataSender dataSender, TraceContext traceContext, ProfilerConfig profilerConfig) {
			this.dataSender = dataSender;
			this.traceContext = traceContext;
			this.profilerConfig = profilerConfig;
		}

		public void run() {
			try {
				JVMInfoThriftDTO jvmInfo = new JVMInfoThriftDTO();
				jvmInfo.setAgentId(traceContext.getAgentId());
				jvmInfo.setDataTime(System.currentTimeMillis());

				activeThread(traceContext, jvmInfo);

				setGCState(jvmInfo);
				setMemoryState(jvmInfo);
				setProcessCPUUsage(jvmInfo);

				dataSender.send(jvmInfo);
			} catch (Exception e) {
				logger.warn("JvmInfo collect error Cause:" + e.getMessage(), e);
			}
		}

		private void activeThread(TraceContext traceContext, JVMInfoThriftDTO jvmInfo) {
//			int activeThread = traceContext.getActiveThreadCounter().getActiveThread();
            int activeThread = 0;
			jvmInfo.setActiveThreadCount(activeThread);
		}

		private void setGCState(JVMInfoThriftDTO jvmInfo) throws Exception {
			List<GarbageCollectorMXBean> list = ManagementFactory.getGarbageCollectorMXBeans();
			if (list.size() == 2) {
				// 제네레이션 기반일 경우 young, old 2개.
				// young.getName() // young gc type
				GarbageCollectorMXBean young = list.get(0);
				jvmInfo.setGc1Count(young.getCollectionCount());
				jvmInfo.setGc1Time(young.getCollectionTime());

				GarbageCollectorMXBean old = list.get(1);
				// old.getName() // old gc type
				jvmInfo.setGc2Count(old.getCollectionCount());
				jvmInfo.setGc2Time(old.getCollectionTime());
			} else {
				// g1 ?
				if (logger.isDebugEnabled()) {
					logger.debug("unknown gc type. gc collector size:" + list.size());
				}
			}
		}

		public void setMemoryState(JVMInfoThriftDTO jvmInfo) throws Exception {
			MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
			MemoryUsage heap = bean.getHeapMemoryUsage();
			MemoryUsage nonHeap = bean.getNonHeapMemoryUsage();

			jvmInfo.setHeapUsed(heap.getUsed());
			jvmInfo.setHeapCommitted(heap.getCommitted());
			jvmInfo.setNonHeapUsed(nonHeap.getUsed());
			jvmInfo.setNonHeapCommitted(nonHeap.getCommitted());
		}

		long previousCpuTime = 0;
		int processorCount = -1;
		boolean processCPUAvailable = true;

		/**
		 * I don't know why should I divide by 10 in this result. But it works.
		 * 
		 * @throws Exception
		 */
		private void setProcessCPUUsage(JVMInfoThriftDTO jvmInfo) throws Exception {
			try {
				if (processCPUAvailable) {
					OperatingSystemMXBean sunOSMBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
					long cpuTime = sunOSMBean.getProcessCpuTime();

					if (processorCount == -1) {
						processorCount = sunOSMBean.getAvailableProcessors();
					}

					if (previousCpuTime != 0) {
						long usedCPUTotal = (cpuTime - previousCpuTime) / 1000000;
						double usedCPU = (0.1D * usedCPUTotal) / (processorCount * profilerConfig.getProfileJvmCollectInterval() / 1000.0);
						jvmInfo.setProcessCPUTime(usedCPU);
					}
					previousCpuTime = cpuTime;

				}
			} catch (IOException e) {
				processCPUAvailable = false;
			}
		}
	}
}

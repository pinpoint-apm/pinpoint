package com.profiler;

import static com.profiler.config.TomcatProfilerConfig.JVM_STAT_GAP;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.profiler.common.dto.thrift.JVMInfoThriftDTO;
import com.profiler.sender.DataSender;
import com.profiler.trace.RequestTracer;
import com.sun.management.OperatingSystemMXBean;

/**
 * System monitor
 * 
 * @author netspider
 * 
 */
@SuppressWarnings("restriction")
public class SystemMonitor {

	private static final Logger logger = Logger.getLogger(SystemMonitor.class.getName());

	private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread t = new Thread(runnable);
			t.setName("HIPPO-SystemMonitor");
			t.setDaemon(true);
			return t;
		}
	});

	public void start() {
		logger.info("Starting system monitor.");
		executor.scheduleAtFixedRate(new Worker(), 5, 5, TimeUnit.SECONDS);
	}

	public void stop() {
		logger.info("Stopping system monitor");
		executor.shutdown();
	}

	private static class Worker implements Runnable {

		private JVMInfoThriftDTO currentDto = new JVMInfoThriftDTO();

		public void run() {
			try {
				currentDto = new JVMInfoThriftDTO();
				currentDto.setAgentHashCode(Agent.getInstance().getAgentHashCode());
				currentDto.setDataTime(System.currentTimeMillis());

				getActiveThreadCount();
				getGCState();
				getMemoryState();
				getProcessCPUUsage();

				DataSender.getInstance().addDataToSend(currentDto);
			} catch (Exception e) {

			}
		}

		private void getActiveThreadCount() throws Exception {
			int currentSize = RequestTracer.getActiveThreadCount();
			currentDto.setActiveThreadCount(currentSize);
		}

		private void getGCState() throws Exception {
			List<GarbageCollectorMXBean> list = ManagementFactory.getGarbageCollectorMXBeans();
			if (list.size() == 2) {
				GarbageCollectorMXBean bean1 = list.get(0);
				currentDto.setGc1Count(bean1.getCollectionCount());
				currentDto.setGc1Time(bean1.getCollectionTime());
				GarbageCollectorMXBean bean2 = list.get(1);
				currentDto.setGc2Count(bean2.getCollectionCount());
				currentDto.setGc2Time(bean2.getCollectionTime());
			}
		}

		public void getMemoryState() throws Exception {
			MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
			MemoryUsage heap = bean.getHeapMemoryUsage();
			MemoryUsage nonHeap = bean.getNonHeapMemoryUsage();

			currentDto.setHeapUsed(heap.getUsed());
			currentDto.setHeapCommitted(heap.getCommitted());
			currentDto.setNonHeapUsed(nonHeap.getUsed());
			currentDto.setNonHeapCommitted(nonHeap.getCommitted());
		}

		long previousCpuTime = 0;
		int processorCount = -1;
		boolean processCPUAvailable = true;

		/**
		 * I don't know why should I divide by 10 in this result. But it works.
		 * - -;
		 * 
		 * @throws Exception
		 */
		private void getProcessCPUUsage() throws Exception {
			try {
				if (processCPUAvailable) {
					OperatingSystemMXBean sunOSMBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
					long cpuTime = sunOSMBean.getProcessCpuTime();

					if (processorCount == -1) {
						processorCount = sunOSMBean.getAvailableProcessors();
					}

					if (previousCpuTime != 0) {
						long usedCPUTotal = (cpuTime - previousCpuTime) / 1000000;
						double usedCPU = (0.1D * usedCPUTotal) / (processorCount * JVM_STAT_GAP / 1000.0);
						currentDto.setProcessCPUTime(usedCPU);
					}
					previousCpuTime = cpuTime;
				}
			} catch (Exception e) {
				e.printStackTrace();
				processCPUAvailable = false;
			}
		}
	}
}

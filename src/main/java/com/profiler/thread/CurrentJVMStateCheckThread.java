package com.profiler.thread;

import static com.profiler.config.TomcatProfilerConfig.JVM_STAT_GAP;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.util.List;

import com.profiler.dto.AgentInfoDTO;
import com.profiler.dto.JVMInfoThriftDTO;
import com.profiler.sender.AgentInfoSender;
import com.profiler.sender.DataSender;
import com.profiler.trace.RequestTracer;
import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public class CurrentJVMStateCheckThread extends Thread {

	private JVMInfoThriftDTO currentDto = new JVMInfoThriftDTO();

	public void run() {
		setHostIPandPort();

		long gap = 0;
		while (true) {
			try {
				if (JVM_STAT_GAP >= gap) {
					long currentMod = System.currentTimeMillis() % 1000;
					Thread.sleep(JVM_STAT_GAP - gap - currentMod + 1);
				}
				long startTime = System.currentTimeMillis();

				sendJVMState();

				long endTime = System.currentTimeMillis();
				gap = endTime - startTime;
			} catch (java.util.concurrent.RejectedExecutionException ree) {
				System.out.println("RejectedExecutionException when sending JVM State info ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method run only one time and send current agent's information.
	 * 
	 * It check's host IP and port. After then make hashCode of IP+Port string.
	 * This hashCode is always different with others.
	 */
	private void setHostIPandPort() {
		AgentInfoDTO.staticPortNumber = AgentInfoDTO.getPortNumberString();

		String hostIP = null;

		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			hostIP = thisIp.getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
			hostIP = "127.0.0.1";
		}

		AgentInfoDTO.staticHostIP = hostIP;

		System.out.println("*** TomcatProfiler : HostIP=" + hostIP + " PortNumbers=" + AgentInfoDTO.staticPortNumber);

		AgentInfoDTO.staticHostHashCode = (hostIP + AgentInfoDTO.staticPortNumber).hashCode();

		AgentInfoSender sender = new AgentInfoSender(true);
		sender.start();
	}

	/**
	 * Every JVM_STAT_GAP time, this method is called
	 * 
	 * @throws Exception
	 */
	private void sendJVMState() throws Exception {
		currentDto = new JVMInfoThriftDTO();
		currentDto.setAgentHashCode(AgentInfoDTO.staticHostHashCode);
		currentDto.setDataTime(System.currentTimeMillis());
		
		getActiveThreadCount();
		getGCState();
		getMemoryState();
		getProcessCPUUsage();
		
		DataSender.getInstance().addDataToSend(currentDto);
	}

	private void getActiveThreadCount() throws Exception {
		int currentSize = RequestTracer.getActiveThreadCount();
		currentDto.setActiveThreadCount(currentSize);
		// JVMDataSender sender=new JVMDataSender(dto);
		// sender.send();
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
	 * I don't know why should I divide by 10 in this result. But it works. - -;
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
				// System.out.println(cpuTime);
				if (previousCpuTime != 0) {
					long usedCPUTotal = (cpuTime - previousCpuTime) / 1000000;
					double usedCPU = (0.1D * usedCPUTotal) / (processorCount * JVM_STAT_GAP / 1000.0);
					currentDto.setProcessCPUTime(usedCPU);
				}
				previousCpuTime = cpuTime;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			processCPUAvailable = false;
		}
	}
}

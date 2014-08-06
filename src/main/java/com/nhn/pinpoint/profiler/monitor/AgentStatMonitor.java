package com.nhn.pinpoint.profiler.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nhn.pinpoint.profiler.monitor.codahale.AgentStatCollectorFactory;
import com.nhn.pinpoint.profiler.monitor.codahale.cpu.CpuLoadCollector;
import com.nhn.pinpoint.profiler.monitor.codahale.gc.GarbageCollector;
import com.nhn.pinpoint.thrift.dto.TAgentStatBatch;
import com.nhn.pinpoint.thrift.dto.TAgentStatSingle;
import com.nhn.pinpoint.thrift.dto.TCpuLoad;
import com.nhn.pinpoint.thrift.dto.TJvmGc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.profiler.sender.DataSender;

/**
 * AgentStat monitor
 * 
 * @author harebox
 * @author hyungil.jeong
 */
public class AgentStatMonitor {

	private static final long DEFAULT_COLLECTION_INTERVAL_MS = 1000 * 5;
	private static final int DEFAULT_NUM_COLLECTIONS_PER_SEND = 6;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final boolean isTrace = logger.isTraceEnabled();
	private final long collectionIntervalMs;
	private final int numCollectionsPerBatch;

	private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new PinpointThreadFactory("Pinpoint-stat-monitor", true));

	private final DataSender dataSender;
	private final String agentId;
	private final AgentStatCollectorFactory agentStatCollectorFactory;
	private final long agentStartTime;

	public AgentStatMonitor(DataSender dataSender, String agentId, long startTime) {
		this(dataSender, agentId, startTime, DEFAULT_COLLECTION_INTERVAL_MS, DEFAULT_NUM_COLLECTIONS_PER_SEND);
	}

	public AgentStatMonitor(DataSender dataSender, String agentId, long startTime, long collectionInterval, int numCollectionsPerBatch) {
		if (dataSender == null) {
			throw new NullPointerException("dataSender must not be null");
		}
		if (agentId == null) {
			throw new NullPointerException("agentId must not be null");
		}
		this.dataSender = dataSender;
		this.agentId = agentId;
		this.agentStartTime = startTime;
		this.collectionIntervalMs = collectionInterval;
		this.numCollectionsPerBatch = numCollectionsPerBatch;
		this.agentStatCollectorFactory = new AgentStatCollectorFactory();
	}

	public void start() {
		long wait = 0;
		CollectJob job = new CollectJob(this.numCollectionsPerBatch);
		executor.scheduleAtFixedRate(job, wait, this.collectionIntervalMs, TimeUnit.MILLISECONDS);
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

	private class CollectJob implements Runnable {

		private final GarbageCollector garbageCollector;
		private final CpuLoadCollector cpuLoadCollector;
		// 어차피 한 개의 쓰레드에서 한 개의 객체만 사용.
		// 멀티쓰레드로 돌릴 일이 생긴다면 바꿔야 함. (그럴 일은 없을 것 같음)
		private final int numStatsPerBatch;
		private int collectCount = 0;
		private List<TAgentStatSingle> agentStats;

		private CollectJob(int numStatsPerBatch) {
			this.garbageCollector = agentStatCollectorFactory.getGarbageCollector();
			this.cpuLoadCollector = agentStatCollectorFactory.getCpuLoadCollector();
			this.numStatsPerBatch = numStatsPerBatch;
			this.agentStats = new ArrayList<TAgentStatSingle>(this.numStatsPerBatch);
		}

		public void run() {
			try {
				final TAgentStatSingle agentStatSingle = collectAgentStat();
				this.agentStats.add(agentStatSingle);
				if (++this.collectCount >= this.numStatsPerBatch) {
					sendAgentStats();
					resetBatchCount();
				}
			} catch (Exception ex) {
				logger.warn("AgentStat collect failed. Caused:{}", ex.getMessage(), ex);
			}
		}

		private TAgentStatSingle collectAgentStat() {
			final TAgentStatSingle agentStatSingle = new TAgentStatSingle();
			agentStatSingle.setTimeStamp(System.currentTimeMillis());
			final TJvmGc gc = garbageCollector.collect();
			agentStatSingle.setGc(gc);
			final TCpuLoad cpuLoad = cpuLoadCollector.collectCpuLoad();
			agentStatSingle.setCpuLoad(cpuLoad);
			if (isTrace) {
				logger.trace("collect agentStat:{}", agentStatSingle);
			}
			return agentStatSingle;
		}

		private void sendAgentStats() {
			// TAgentStat 객체를 준비한다.
			// TODO TAgentStat을 재활용시 datasender가 별도의 thread이기 때문에.
			// multithread문제가 생길수 있음.
			final TAgentStatBatch agentStatBatch = new TAgentStatBatch();
			agentStatBatch.setAgentId(agentId);
			agentStatBatch.setStartTimestamp(agentStartTime);
			agentStatBatch.setAgentStats(this.agentStats);
			if (isTrace) {
				logger.trace("collect agentStat:{}", agentStatBatch);
			}
			dataSender.send(agentStatBatch);
		}

		private void resetBatchCount() {
			this.collectCount = 0;
			// 위와 마찬가지로 agentStats 리스트 재활용시, datasender가 별도의 thread이기 때문에,
			// send하기 전에 리스트가 변경될 수 있음. 따라서 새로운 리스트를 만들어준다.
			this.agentStats = new ArrayList<TAgentStatSingle>(this.numStatsPerBatch);
		}
	}

}

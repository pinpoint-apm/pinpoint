package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.cluster.zookeeper.exception.TimeoutException;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.DeleteJob;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.Job;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.UpdateJob;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.collector.receiver.tcp.AgentPropertiesType;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.nhn.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class ZookeeperLatestJobWorker implements Runnable {

	private static final String PREFIX_ROOT_ZNODE = "/pinpoint-cluster";
	private static final String PATH_SEPRATOR = "/";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Object lock = new Object();

	private final AtomicInteger workerState;
	private final Thread workerThread;

	private final String identifier = ManagementFactory.getRuntimeMXBean().getName();
	private final AtomicInteger sequntialId = new AtomicInteger(0);

	private final ZookeeperClient zookeeperClient;

	// 메시지가 사라지면 ChannelContext 역시 사라짐
	private final Map<ChannelContext, Job> latestJobRepository = new HashMap<ChannelContext, Job>();

	// 들어온 ChannelContext는 계속 유지됨
	private final Map<ChannelContext, String> znodeMappingRepository = new HashMap<ChannelContext, String>();

	private final BlockingQueue<Job> leakJobQueue = new LinkedBlockingQueue<Job>();

	// 등록순서
	// 순서대로 작업은 반드시 Worker에서만 돌아가기 때문에 동시성은 보장됨

	public ZookeeperLatestJobWorker(ZookeeperClient zookeeperClient) {
		// TODO Auto-generated constructor stub
		this.zookeeperClient = zookeeperClient;

		this.workerState = new AtomicInteger(0);

		final ThreadFactory threadFactory = new PinpointThreadFactory(this.getClass().getSimpleName(), true);
		this.workerThread = threadFactory.newThread(this);
	}

	public void start() {
		switch (this.workerState.get()) {
		case 0:
			if (this.workerState.compareAndSet(0, 1))
				logger.info("{} will be started", this.getClass().getSimpleName());
			this.workerThread.start();
			break;
		case 1:
			logger.info("{} already started.", this.getClass().getSimpleName());
			break;
		case 2:
			throw new IllegalStateException("Already stopped");
		default:
			throw new Error("Invalid WorkerState");
		}
	}

	public void stop() {
		if (!(this.workerState.compareAndSet(1, 2))) {
			logger.info("{} already stopped.", this.getClass().getSimpleName());
			this.workerState.set(2);
			return;
		}

		logger.info("{} will be stopped", this.getClass().getSimpleName());

		boolean interrupted = false;
		while (this.workerThread.isAlive()) {
			this.workerThread.interrupt();
			try {
				this.workerThread.join(100L);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}

		return;
	}

	@Override
	public void run() {

		// 고민할 것
		// 이벤트 삭제가 안될떄 spinlock 고민해야함
		// 이벤트 발생시 처리가 안될 경우 실제 등록이 안되어이는 ChannelContext가 남아있을 수 있음 이 경우
		// Manager에서
		// 종료가 될 경우 처리해주긴 하지만 LeakDetector같은게 있으면 더 좋을듯 안되면 (1분마다 확인등도 괜찮을듯)
		while (workerState.get() == 1) {
			boolean eventCreated = await(60000, 200);
			if (workerState.get() != 1) {
				break;
			}

			// 이벤트 발생시 이벤트 처리
			// 이벤트 발생하지 않을 경우 leack ChannelContext 확인 및 처리
			if (eventCreated) {
				// ConcurrentModificationException 발생 피하기 위해서
				Iterator<ChannelContext> keyIterator = getLatestJobRepositoryKeyIterator();

				while (keyIterator.hasNext()) {
					ChannelContext channelContext = keyIterator.next();
					Job job = getJob(channelContext);

					if (job == null) {
						continue;
					}

					if (job instanceof UpdateJob) {
						handleUpdate((UpdateJob) job);
					} else if (job instanceof DeleteJob) {
						handleDelete((DeleteJob) job);
					}
				}
			} else {
				logger.debug("LeackDetector Start.");

				// while 걸자 
				while (true) {
					Job job = leakJobQueue.poll();
					if (job == null) {
						break;
					}
					
					if (job instanceof UpdateJob) {
						putJob(new UpdateJob(job.getChannelContext(), 0, ((UpdateJob) job).getContents()));
					}
				}

				List<ChannelContext> currentChannelContextList = getRegisteredChannelContextList();
				for (ChannelContext channelContext : currentChannelContextList) {
					if (PinpointServerSocketStateCode.isFinished(channelContext.getCurrentStateCode())) {
						logger.info("LeackDetector Find Leak ChannelContext={}.", channelContext);
						putJob(new DeleteJob(channelContext));
					}
				}
				
			}
		}

		logger.info("{} stopped", this.getClass().getSimpleName());
	}

	public boolean handleUpdate(UpdateJob job) {
		ChannelContext channelContext = job.getChannelContext();

		PinpointServerSocketStateCode code = channelContext.getCurrentStateCode();
		if (PinpointServerSocketStateCode.isFinished(code)) {
			putJob(new DeleteJob(channelContext));
			return false;
		}

		// 동시성에 문제 없게 하자
		String uniquePath = getUniquePath(channelContext, true);

		try {
			if (zookeeperClient.exists(uniquePath)) {
				zookeeperClient.setData(uniquePath, job.getContents());
			} else {
				zookeeperClient.createPath(uniquePath);
				zookeeperClient.createNode(uniquePath, job.getContents());
				logger.info("Registed Zookeeper UniqPath = {}", uniquePath);
			}
			return true;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			if (e instanceof TimeoutException) {
				job.incrementCurrentRetryCount();
				putJob(job);
			}
		}

		return false;
	}

	public boolean handleDelete(Job job) {
		ChannelContext channelContext = job.getChannelContext();

		String uniquePath = getUniquePath(channelContext, false);

		if (uniquePath == null) {
			logger.info("Already Delete Zookeeper UniqPath ChannelContext = {}.", channelContext);
			return true;
		}

		try {
			if (zookeeperClient.exists(uniquePath)) {
				zookeeperClient.delete(uniquePath);
				logger.info("Unregisted Zookeeper UniqPath = {}", uniquePath);
			}
			removeUniquePath(channelContext);
			return true;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			if (e instanceof TimeoutException) {
				job.incrementCurrentRetryCount();
				putJob(job);
			}
		}

		return false;
	}

	public byte[] getData(ChannelContext channelContext) {
		String uniquePath = getUniquePath(channelContext, false);

		if (uniquePath == null) {
			logger.info("Can't find suitable UniqPath ChannelContext = {}.", channelContext);
			return null;
		}

		try {
			return zookeeperClient.getData(uniquePath);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		return null;
	}

	public List<ChannelContext> getRegisteredChannelContextList() {
		synchronized (znodeMappingRepository) {
			return new ArrayList<ChannelContext>(znodeMappingRepository.keySet());
		}
	}

	/**
	 * 파라미터의 대기시간동안 이벤트가 일어날 경우 true 일어나지 않을 경우 false
	 * 
	 * @param waitTimeMillis
	 * @return
	 */
	private boolean await(long waitTimeMillis, long waitUnitTimeMillis) {
		synchronized (lock) {
			long waitTime = waitTimeMillis;
			long waitUnitTime = waitUnitTimeMillis;
			if (waitTimeMillis < 1000) {
				waitTime = 1000;
			}
			if (waitUnitTimeMillis < 100) {
				waitUnitTime = 100;
			}

			long startTimeMillis = System.currentTimeMillis();

			while (latestJobRepository.size() == 0 && !isOverWaitTime(waitTime, startTimeMillis) && workerState.get() == 1) {
				try {
					lock.wait(waitUnitTime);
				} catch (InterruptedException e) {

				}
			}

			if (isOverWaitTime(waitTime, startTimeMillis)) {
				return false;
			}

			return true;
		}
	}

	private boolean isOverWaitTime(long waitTimeMillis, long startTimeMillis) {
		return waitTimeMillis < (System.currentTimeMillis() - startTimeMillis);
	}

	private Iterator<ChannelContext> getLatestJobRepositoryKeyIterator() {
		synchronized (lock) {
			return latestJobRepository.keySet().iterator();
		}
	}

	// 상당히 민감한 api임 Runnable에서만 사용해야함
	private Job getJob(ChannelContext channelContext) {
		synchronized (lock) {
			Job job = latestJobRepository.remove(channelContext);
			return job;
		}

	}

	public void putJob(Job job) {
		if (job.getMaxRetryCount() < job.getCurrentRetryCount()) {
			if (logger.isInfoEnabled()) {
				logger.warn("Leack Job Queue Register Job={}.", job);
			}
			leakJobQueue.add(job);
			return;
		}

		synchronized (lock) {
			ChannelContext channelContext = job.getChannelContext();

			latestJobRepository.put(channelContext, job);
			lock.notifyAll();
		}
	}

	private String getUniquePath(ChannelContext channelContext, boolean create) {
		synchronized (znodeMappingRepository) {
			String zNodePath = znodeMappingRepository.get(channelContext);

			if (!create) {
				return zNodePath;
			}

			if (zNodePath == null) {
				Map<Object, Object> agentProperties = channelContext.getChannelProperties();
				final String applicationName = MapUtils.getString(agentProperties, AgentPropertiesType.APPLICATION_NAME.getName());
				final String agentId = MapUtils.getString(agentProperties, AgentPropertiesType.AGENT_ID.getName());

				if (StringUtils.isEmpty(applicationName) || StringUtils.isEmpty(agentId)) {
					return null;
				}

				String path = PREFIX_ROOT_ZNODE + "/" + applicationName + "/" + agentId;
				String zNodeName = createUniqueZnodeName();

				zNodePath = bindingPathAndZnode(path, zNodeName);
				znodeMappingRepository.put(channelContext, zNodePath);

				logger.info("Created Zookeeper UniqPath = {}", zNodePath);
			}

			return zNodePath;
		}
	}

	private void removeUniquePath(ChannelContext channelContext) {
		synchronized (znodeMappingRepository) {
			String zNodePath = znodeMappingRepository.remove(channelContext);
			if (zNodePath != null) {
				logger.info("Deleted Zookeeper UniqPath = {}", zNodePath);
			}
		}
	}

	private String createUniqueZnodeName() {
		return identifier + "_" + sequntialId.getAndIncrement();
	}

	private String bindingPathAndZnode(String path, String znodeName) {
		StringBuilder fullPath = new StringBuilder();

		fullPath.append(path);
		if (!path.endsWith(PATH_SEPRATOR)) {
			fullPath.append(PATH_SEPRATOR);
		}
		fullPath.append(znodeName);

		return fullPath.toString();
	}

}

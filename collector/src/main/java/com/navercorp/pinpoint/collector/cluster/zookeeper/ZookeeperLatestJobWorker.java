package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.cluster.WorkerState;
import com.nhn.pinpoint.collector.cluster.WorkerStateContext;
import com.nhn.pinpoint.collector.cluster.zookeeper.exception.TimeoutException;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.DeleteJob;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.Job;
import com.nhn.pinpoint.collector.cluster.zookeeper.job.UpdateJob;
import com.nhn.pinpoint.collector.receiver.tcp.AgentHandShakePropertyType;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.server.ChannelContext;
import com.nhn.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.nhn.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class ZookeeperLatestJobWorker implements Runnable {

	private static final Charset charset = Charset.forName("UTF-8");

	private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
	private static final String PINPOINT_COLLECTOR_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/collector";

	private static final String PATH_SEPRATOR = "/";
	private static final String PROFILER_SEPERATOR = "\r\n";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Object lock = new Object();

	private final WorkerStateContext workerState;
	private final Thread workerThread;

	private final String collectorUniqPath;
	
	private final ZookeeperClient zookeeperClient;

	// 메시지가 사라지면 ChannelContext 역시 사라짐
	private final ConcurrentHashMap<ChannelContext, Job> latestJobRepository = new ConcurrentHashMap<ChannelContext, Job>();

	// Worker에 들어온 ChannelContext를 관리하는 저장소
	private final CopyOnWriteArrayList<ChannelContext> channelContextRepository = new CopyOnWriteArrayList<ChannelContext>();

	private final BlockingQueue<Job> leakJobQueue = new LinkedBlockingQueue<Job>();

	// 등록순서
	// 순서대로 작업은 반드시 Worker에서만 돌아가기 때문에 동시성은 보장됨

	public ZookeeperLatestJobWorker(ZookeeperClient zookeeperClient, String serverIdentifier) {
		// TODO Auto-generated constructor stub
		this.zookeeperClient = zookeeperClient;

		this.workerState = new WorkerStateContext();

		this.collectorUniqPath = bindingPathAndZnode(PINPOINT_COLLECTOR_CLUSTER_PATH, serverIdentifier);
		
		final ThreadFactory threadFactory = new PinpointThreadFactory(this.getClass().getSimpleName(), true);
		this.workerThread = threadFactory.newThread(this);
	}

	public void start() {
		switch (this.workerState.getCurrentState()) {
		case NEW:
			if (this.workerState.changeStateInitializing()) {
				logger.info("{} initialization started.", this.getClass().getSimpleName());
				workerState.changeStateStarted();

				this.workerThread.start();
				logger.info("{} initialization completed.", this.getClass().getSimpleName());

				break;
			}
		case INITIALIZING:
			logger.info("{} already initializing.", this.getClass().getSimpleName());
			break;
		case STARTED:
			logger.info("{} already started.", this.getClass().getSimpleName());
			break;
		case DESTROYING:
			throw new IllegalStateException("Already destroying.");
		case STOPPED:
			throw new IllegalStateException("Already stopped.");
		case ILLEGAL_STATE:
			throw new IllegalStateException("Invalid State.");
		}
	}

	public void stop() {
		if (!(this.workerState.changeStateDestroying())) {
			WorkerState state = this.workerState.getCurrentState();

			logger.info("{} already {}.", this.getClass().getSimpleName(), state.toString());
			return;
		}

		logger.info("{} destorying started.", this.getClass().getSimpleName());
		boolean interrupted = false;
		while (this.workerThread.isAlive()) {
			this.workerThread.interrupt();
			try {
				this.workerThread.join(100L);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}

		this.workerState.changeStateStoped();
		logger.info("{} destorying completed.", this.getClass().getSimpleName());

		return;
	}

	@Override
	public void run() {

		// 고민할 것
		// 이벤트 삭제가 안될떄 spinlock 고민해야함
		// 이벤트 발생시 처리가 안될 경우 실제 등록이 안되어이는 ChannelContext가 남아있을 수 있음 이 경우
		while (workerState.isStarted()) {
			boolean eventCreated = await(60000, 200);
			if (!workerState.isStarted()) {
				break;
			}

			// 이벤트 발생시 이벤트 처리
			// 이벤트 발생하지 않을 경우 leak ChannelContext 확인 및 처리
			if (eventCreated) {
				// ConcurrentModificationException 발생 피하기 위해서
				Iterator<ChannelContext> keyIterator = getLatestJobRepositoryKeyIterator();

				while (keyIterator.hasNext()) {
					ChannelContext channelContext = keyIterator.next();
					Job job = getJob(channelContext);
					if (job == null) {
						continue;
					}
					
					logger.info("Worker execute job({}).", job);
					
					if (job instanceof UpdateJob) {
						handleUpdate((UpdateJob) job);
					} else if (job instanceof DeleteJob) {
						handleDelete((DeleteJob) job);
					}
				}
			} else {
				// 삭제 타이밍이 잘 안맞을 경우 메시지 유실이 발생할 가능성이 있어서 유실 Job 처리
				logger.debug("LeakDetector Start.");

				while (true) {
					Job job = leakJobQueue.poll();
					if (job == null) {
						break;
					}

					if (job instanceof UpdateJob) {
						putRetryJob(new UpdateJob(job.getChannelContext(), 1, ((UpdateJob) job).getContents()));
					}
				}

				for (ChannelContext channelContext : channelContextRepository) {
					if (PinpointServerSocketStateCode.isFinished(channelContext.getCurrentStateCode())) {
						logger.info("LeakDetector Find Leak ChannelContext={}.", channelContext);
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

		try {
			String addContents = createProfilerContents(channelContext);

			if (zookeeperClient.exists(collectorUniqPath)) {
				byte[] contents = zookeeperClient.getData(collectorUniqPath);
				
				String data = addIfAbsentContents(new String(contents, charset), addContents);
				zookeeperClient.setData(collectorUniqPath, data.getBytes(charset));
			} else {
				zookeeperClient.createPath(collectorUniqPath);
				
				// data가 중요한 것이라면 NODE가 존재해도 에러를 반환해야 한다. 
				zookeeperClient.createNode(collectorUniqPath, addContents.getBytes(charset));
			}
			return true;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			if (e instanceof TimeoutException) {
				putRetryJob(job);
			}
		}

		return false;
	}

	public boolean handleDelete(Job job) {
		ChannelContext channelContext = job.getChannelContext();

		try {
			if (zookeeperClient.exists(collectorUniqPath)) {
				byte[] contents = zookeeperClient.getData(collectorUniqPath);
				
				String removeContents = createProfilerContents(channelContext);
				String data = removeIfExistContents(new String(contents, charset), removeContents);
				
				zookeeperClient.setData(collectorUniqPath, data.getBytes(charset));
			}
			channelContextRepository.remove(channelContext);
			return true;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			if (e instanceof TimeoutException) {
				putRetryJob(job);
			}
		}

		return false;
	}

	public byte[] getClusterData() {
		try {
			return zookeeperClient.getData(collectorUniqPath);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		return null;
	}

	public List<ChannelContext> getRegisteredChannelContextList() {
		return new ArrayList<ChannelContext>(channelContextRepository);
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

			while (latestJobRepository.size() == 0 && !isOverWaitTime(waitTime, startTimeMillis) && workerState.isStarted()) {
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
		ChannelContext channelContext = job.getChannelContext();
		if (!checkRequiredProperties(channelContext)) {
			return;
		}
		
		synchronized (lock) {
			channelContextRepository.addIfAbsent(channelContext);
			latestJobRepository.put(channelContext, job);
			lock.notifyAll();
		}
	}
	
	private void putRetryJob(Job job) {
		job.incrementCurrentRetryCount();

		if (job.getMaxRetryCount() < job.getCurrentRetryCount()) {
			if (logger.isInfoEnabled()) {
				logger.warn("Leak Job Queue Register Job={}.", job);
			}
			leakJobQueue.add(job);
			return;
		}
		
		ChannelContext channelContext = job.getChannelContext();

		synchronized (lock) {
			latestJobRepository.putIfAbsent(channelContext, job);
			lock.notifyAll();
		}
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

	private boolean checkRequiredProperties(ChannelContext channelContext) {
		Map<Object, Object> agentProperties = channelContext.getChannelProperties();
		final String applicationName = MapUtils.getString(agentProperties, AgentHandShakePropertyType.APPLICATION_NAME.getName());
		final String agentId = MapUtils.getString(agentProperties, AgentHandShakePropertyType.AGENT_ID.getName());
		final Long startTimeStampe = MapUtils.getLong(agentProperties, AgentHandShakePropertyType.START_TIMESTAMP.getName());
		
		if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId) || startTimeStampe == null || startTimeStampe <= 0) {
			logger.warn("ApplicationName({}) and AgnetId({}) and startTimeStampe({}) may not be null.", applicationName, agentId);
			return false;
		}
		
		return true;
	}
	
	private String createProfilerContents(ChannelContext channelContext) {
		StringBuilder profilerContents = new StringBuilder();
		
		Map<Object, Object> agentProperties = channelContext.getChannelProperties();
		final String applicationName = MapUtils.getString(agentProperties, AgentHandShakePropertyType.APPLICATION_NAME.getName());
		final String agentId = MapUtils.getString(agentProperties, AgentHandShakePropertyType.AGENT_ID.getName());
		final Long startTimeStampe = MapUtils.getLong(agentProperties, AgentHandShakePropertyType.START_TIMESTAMP.getName());
		
		if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId) || startTimeStampe == null || startTimeStampe <= 0) {
			logger.warn("ApplicationName({}) and AgnetId({}) and startTimeStampe({}) may not be null.", applicationName, agentId);
			return StringUtils.EMPTY;
		}
		
		// 구분자 : 허용하지 않기 떄문 -,_,. 허용
		profilerContents.append(applicationName);
		profilerContents.append(":");
		profilerContents.append(agentId);
		profilerContents.append(":");
		profilerContents.append(startTimeStampe);
		
		return profilerContents.toString();
	}

	private String addIfAbsentContents(String contents, String addContents) {
		String[] allContents = contents.split(PROFILER_SEPERATOR);
		
		for (String eachContent : allContents) {
			if (StringUtils.equals(eachContent.trim(), addContents.trim())) {
				return contents;
			}
		}
		
		return contents + PROFILER_SEPERATOR + addContents;
	}
	
	private String removeIfExistContents(String contents, String removeContents) {
		StringBuilder newContents = new StringBuilder(contents.length());
		
		String[] allContents = contents.split(PROFILER_SEPERATOR);

		Iterator<String> stringIterator = Arrays.asList(allContents).iterator();
		
		while (stringIterator.hasNext()) {
			String eachContent = stringIterator.next();
			
			if (StringUtils.isBlank(eachContent)) {
				continue;
			}
			
			if (!StringUtils.equals(eachContent.trim(), removeContents.trim())) {
				newContents.append(eachContent);

				if (stringIterator.hasNext()) {
					newContents.append(PROFILER_SEPERATOR);
				}
			}
		}

		return newContents.toString();
	}
	
}

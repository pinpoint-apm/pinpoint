package com.nhn.pinpoint.collector.cluster.zookeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.cluster.WebClusterPoint;
import com.nhn.pinpoint.collector.cluster.WorkerState;
import com.nhn.pinpoint.collector.cluster.WorkerStateContext;
import com.nhn.pinpoint.collector.cluster.zookeeper.exception.ConnectionException;
import com.nhn.pinpoint.common.util.NetUtils;
import com.nhn.pinpoint.common.util.PinpointThreadFactory;

/**
 * @author koo.taejin <kr14910>
 */
public class ZookeeperWebClusterManager implements Runnable {

	// 콜렉터 에서는 무한 retry를 해도됨
	// RETRY_INTERVAL을 받게만 하면 될듯
	private static final int DEFAULT_RETRY_INTERVAL = 60000;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final GetAndRegisterTask getAndRegisterTask = new GetAndRegisterTask();
	private final StopTask stopTask = new StopTask();

	private final ZookeeperClient client;
	private final WebClusterPoint webClusterPoint;
	private final String zNodePath;

	private final AtomicBoolean retryMode = new AtomicBoolean(false);

	private final BlockingQueue<Task> queue = new LinkedBlockingQueue<Task>(1);

	private final WorkerStateContext workerState;
	private final Thread workerThread;

	// private final Timer timer;

	// Worker + Job 등록
	// Job이 포함되면 실행. Job성공시 이후 Job 모두 삭제
	// 먼가 이상한 형태의 자료구조가 필요한거 같은데....

	public ZookeeperWebClusterManager(ZookeeperClient client, String zookeeperClusterPath, String serverIdentifier, WebClusterPoint clusterPoint) {
		this.client = client;

		this.webClusterPoint = clusterPoint;
		this.zNodePath = zookeeperClusterPath;

		this.workerState = new WorkerStateContext();

		final ThreadFactory threadFactory = new PinpointThreadFactory(this.getClass().getSimpleName(), true);
		this.workerThread = threadFactory.newThread(this);
	}

	public void start() {
		switch (this.workerState.getCurrentState()) {
			case NEW:
				if (this.workerState.changeStateInitializing()) {
					logger.info("{} initialization started.", this.getClass().getSimpleName());
					this.workerThread.start();
					
					workerState.changeStateStarted();
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

		queue.offer(stopTask);

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

	// NoNode인 경우 Node생성후 재 호출
	// Timeout인 경우 스케쥴 걸어서 재요청
	// 그외는 그대로 둠
	public void handleAndRegisterWatcher(String path) {
		if (workerState.isStarted()) {
			if (zNodePath.equals(path)) {
				boolean offerSuccess = queue.offer(getAndRegisterTask);

				if (!offerSuccess) {
					logger.info("Message Queue is Full.");
				}
			} else {
				logger.info("Invald Path {}.", path);
			}
		} else {
			WorkerState state = this.workerState.getCurrentState();
			logger.info("{} invalid state {}.", this.getClass().getSimpleName(), state.toString());
			return;
		}
	}

	@Override
	public void run() {
		while (workerState.isStarted()) {
			Task task = null;

			try {
				task = queue.poll(DEFAULT_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.debug(e.getMessage(), e);
			}

			if (!workerState.isStarted()) {
				break;
			}

			if (task == null) {
				if (retryMode.get()) {
					boolean success = getAndRegisterTask.handleAndRegisterWatcher0();
					if (success) {
						retryMode.compareAndSet(true, false);
					}
				}
			} else if (task instanceof GetAndRegisterTask) {
				boolean success = ((GetAndRegisterTask) task).handleAndRegisterWatcher0();
				if (!success) {
					retryMode.compareAndSet(false, true);
				}
			} else if (task instanceof StopTask) {
				break;
			}
		}

		logger.info("{} stopped", this.getClass().getSimpleName());
	}

	interface Task {

	}

	class GetAndRegisterTask implements Task {

		private boolean handleAndRegisterWatcher0() {
			boolean needNotRetry = false;
			try {

				if (!client.exists(zNodePath)) {
					client.createPath(zNodePath, true);
				}

				List<String> childNodeList = client.getChildrenNode(zNodePath, true);
				List<InetSocketAddress> clusterAddressList = NetUtils.toInetSocketAddressLIst(childNodeList);

				List<InetSocketAddress> addressList = webClusterPoint.getWebClusterList();

				logger.info("Handle register and remove Task. Current Address List = {}, Cluster Address List = {}", addressList, clusterAddressList);
				
				for (InetSocketAddress clusterAddress : clusterAddressList) {
					if (!addressList.contains(clusterAddress)) {
						webClusterPoint.connectPointIfAbsent(clusterAddress);
					}
				}

				for (InetSocketAddress address : addressList) {
					if (!clusterAddressList.contains(address)) {
						webClusterPoint.disconnectPoint(address);
					}
				}

				needNotRetry = true;
				return needNotRetry;
			} catch (Exception e) {
				if (!(e instanceof ConnectionException)) {
					needNotRetry = true;
				}
			}

			return needNotRetry;
		}
	}

	static class StopTask implements Task {

	}

}

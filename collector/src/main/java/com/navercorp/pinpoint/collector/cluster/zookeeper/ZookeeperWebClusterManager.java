/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster.zookeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.cluster.WebCluster;
import com.navercorp.pinpoint.collector.cluster.WorkerState;
import com.navercorp.pinpoint.collector.cluster.WorkerStateContext;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.ConnectionException;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;

/**
 * @author koo.taejin
 */
public class ZookeeperWebClusterManager implements Runnable {

    // it is okay for the collector to retry indefinitely, as long as RETRY_INTERVAL is set reasonably
    private static final int DEFAULT_RETRY_INTERVAL = 60000

	private final Logger logger = LoggerFactory.getLogger(this.getClass    ));

	private final GetAndRegisterTask getAndRegisterTask = new GetAndRegiste    Task();
	private final StopTask stopTask = new S    opTask();

	private final Zookeeper    lient client;
	private final WebCl    ster webCluster;
	private fina     String zNodePath;

	private final AtomicBoolean retryMode = new    AtomicBoolean(false);

	private final BlockingQueue<Task> queue = new Link    dBlockingQueue<Task>(1);

	private final Wo    kerStateContext workerState;
	pri    ate final Thread workerThrea    ;

	// private final T    mer timer;

	// Register Worker + Job
	// synchronize current status with Z    okeeper when an event(job) is triggered.
	// (the number of events does not matter as long as a single event is t    iggered - subsequent events may be ignored)
	public ZookeeperWebClusterManager(ZookeeperClient client, String zookeeperClusterPath, St       ing serverIdentifi       r, WebCluster webCluster)       {
		this.client = client;

		this.w       bCluster = webCluster;
		this.zNodePath =       zookeeperClusterPath;

		this.workerState = new WorkerStateContext();

		final ThreadFactory thre       dFactory = new PinpointThreadFactory(this.getC        ss().getSimpleName(       , true);
		this.workerThread = threadFact          ry.             ewThread(this);
	}

	public void start()
		switch (this.workerState.getCurrentState()) {
			case NEW:
			                if (this.workerS                               ate.changeStat                Initializing()) {
					logger.info("{} initialization started.", th                                     s.g             tClass().getSimpleName());
					this.workerThread.start();
					
                      				wo             kerState.changeStateStarted();
					logger.info("{} initiali                      ation com             leted.", this.getClass().getSimpleName());
				          break;
             			}
			case INITIALIZING:
				logger.info("          } already ini             ializing.", this.getClass().getSimpleName(             );
				break;
			       ase STARTED:
				logger.info("{} already start          d.", this.getClass().getSimpleName());
				break;                   			case DESTROYING:
				throw new IllegalStateException("Already destr          y             ng.");
			case STOPPED:
				throw new IllegalStateException("Already stopped.");
			case ILLEGAL_STATE:
				throw new IllegalStateException("Invalid State.");
		}
	}

	public void stop() {
		if (!(this.workerState.changeStateDestroying())) {
			Worke       State state = this.workerState.ge          CurrentState();
			
			l                      gger.info("{} already          {}.", this.getClass().getSim             leName(), s                      ate.toString());
			return;
	       }

		logger.info("{} destorying started.", this.getClass().getSimpleN        e());

        final boolean stopOffer = queue.of       er(stopTask);
        if (          stopOffer) {
                        logger.warn("Insert stopTask failed.");
        }

                    boolea                 interrupted = false;
		whil                                   (this.workerThread.isAliv                          )) {
			this.workerThread.interrupt();
			try {
	          		this.workerThread.join(100L);
			} catch (InterruptedException e) {
				interru             ted =     rue;
			}
		}

		    his.workerState.changeStateStopped();
		logger.info("{} d    storying completed.", this.get       lass().getSimpleName());
	}

          public void                       andleAndRegisterWatcher(String path) {
		if (workerState.          sStarted()) {
			if (zNodePa             h.equals(path)) {
				fi                   al boolean offerSucces                                = queue.             ffer(getAndReg                sterTask);
				if (!offerSuccess) {
					logger.info("M                ssag                    Queue is Full.");
				}
                                     		} else {
				logger.info("             nvald Path {}.", path);
			}
		} else {
			WorkerState state = thi             .worker                tate.getCurrentState();
			                      ogger.info("{} invalid sta                                  e {}.", this.getClass().getSimpleName(), state.to        ring());
		}
	}        	@Override
	public void run() {
	    // i        the node does not exist, create a node a          d retry.
	    // retry                         timeout as well.
		whil                 (workerState.isStarted()                          {
			Task task = null;

			try {
				task = queue.poll(             EFAULT_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
	             		logger.debug(e.getMessage(), e);
			}

			if (!workerState.i             Started()) {
				break;
			}

			if (task == null) {
				if (retryMode.get()) {
					boolean success = getAndRegisterTask.hand                         eAndRegisterWatcher0();
					if (success) {
		                			retryMode.compareAndSet(true, f                   lse);
					}
				}
			} else if (tas                                         instanceof GetAndRegister                ask) {
				boolean success = ((Get                   ndRegisterTask) task).ha                                                    dleAndRegist          rWatcher0();
				             f (!success) {
					retryMode.comp                reAndSet(f                               lse,              rue);
				}
			} else if (task instan    eof StopTask) {
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

				List<InetSocketAddress> addressList = webCluster.getWebClusterList();

				logger.info("Handle register and remove Task. Current Address List = {}, Cluster Address List = {}", addressList, clusterAddressList);
				
				for (InetSocketAddress clusterAddress : clusterAddressList) {
					if (!addressList.contains(clusterAddress)) {
						webCluster.connectPointIfAbsent(clusterAddress);
					}
				}

				for (InetSocketAddress address : addressList) {
					if (!clusterAddressList.contains(address)) {
						webCluster.disconnectPoint(address);
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

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

import com.navercorp.pinpoint.collector.cluster.connection.ClusterConnectionManager;
import com.navercorp.pinpoint.collector.cluster.connection.CollectorClusterConnectionManager;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.ConnectionException;
import com.navercorp.pinpoint.common.util.NetUtils;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonState;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonStateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author koo.taejin
 * @author minwoo.jung
 */
public class ZookeeperClusterManager {

    // it is okay for the collector to retry indefinitely, as long as RETRY_INTERVAL is set reasonably
    private static final int DEFAULT_RETRY_INTERVAL = 60000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GetAndRegisterTask getAndRegisterTask = new GetAndRegisterTask();
    private final StopTask stopTask = new StopTask();

    private final ZookeeperClient client;
    private final ClusterConnectionManager clusterConnectionManager;
    private final String zNodePath;

    private final AtomicBoolean retryMode = new AtomicBoolean(false);

    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>(1);

    private final CommonStateContext workerState;
    private final Thread workerThread;

    // private final Timer timer;

    // Register Worker + Job
    // synchronize current status with Zookeeper when an event(job) is triggered.
    // (the number of events does not matter as long as a single event is triggered - subsequent events may be ignored)
    public ZookeeperClusterManager(ZookeeperClient client, String zookeeperClusterPath, ClusterConnectionManager clusterConnectionManager) {
        this.client = client;

        this.clusterConnectionManager = clusterConnectionManager;
        this.zNodePath = zookeeperClusterPath;

        this.workerState = new CommonStateContext();

        final ThreadFactory threadFactory = new PinpointThreadFactory(this.getClass().getSimpleName(), true);
        this.workerThread = threadFactory.newThread(new Worker());
    }

    public void start() {
        switch (this.workerState.getCurrentState()) {
            case NEW:
                if (this.workerState.changeStateInitializing()) {
                    logger.info("{} initialization started.", this.getClass().getSimpleName());
                    this.workerThread.start();

                    workerState.changeStateStarted();
                    logger.info("{} initialization completed.", this.getClass().getSimpleName());

                    if (clusterConnectionManager != null) {
                        clusterConnectionManager.start();
                    }

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
            CommonState state = this.workerState.getCurrentState();

            logger.info("{} already {}.", this.getClass().getSimpleName(), state.toString());
            return;
        }

        logger.info("{} destroying started.", this.getClass().getSimpleName());

        if (clusterConnectionManager != null) {
            clusterConnectionManager.stop();
        }

        final boolean stopOffer = queue.offer(stopTask);
        if (!stopOffer) {
            logger.warn("Insert stopTask failed.");
        }

        boolean interrupted = false;
        while (this.workerThread.isAlive()) {
            this.workerThread.interrupt();
            try {
                this.workerThread.join(100L);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        this.workerState.changeStateStopped();
        logger.info("{} destroying completed.", this.getClass().getSimpleName());
    }

    public void handleAndRegisterWatcher(String path) {
        if (workerState.isStarted()) {
            if (zNodePath.equals(path)) {
                final boolean offerSuccess = queue.offer(getAndRegisterTask);
                if (!offerSuccess) {
                    logger.info("Message Queue is Full.");
                }
            } else {
                logger.info("Invalid Path {}.", path);
            }
        } else {
            CommonState state = this.workerState.getCurrentState();
            logger.info("{} invalid state {}.", this.getClass().getSimpleName(), state.toString());
        }
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            // if the node does not exist, create a node and retry.
            // retry on timeout as well.
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

    }


    interface Task {

    }

    @SuppressWarnings("SuspiciousMethodCalls")
    class GetAndRegisterTask implements Task {

        @SuppressWarnings("SuspiciousMethodCalls")
        private boolean handleAndRegisterWatcher0() {
            boolean needNotRetry = false;
            try {

                if (!client.exists(zNodePath)) {
                    client.createPath(zNodePath, true);
                }

                List<String> childNodeList = client.getChildrenNode(zNodePath, true);
                List<InetSocketAddress> clusterAddressList = NetUtils.toInetSocketAddressLIst(childNodeList);

                List<SocketAddress> addressList = clusterConnectionManager.getConnectedAddressList();

                logger.info("Handle register and remove Task. Current Address List = {}, Cluster Address List = {}", addressList, clusterAddressList);

                for (InetSocketAddress clusterAddress : clusterAddressList) {
                    if (!addressList.contains(clusterAddress)) {
                        clusterConnectionManager.connectPointIfAbsent(clusterAddress);
                    }
                }

                for (SocketAddress address : addressList) {
                    //noinspection SuspiciousMethodCalls,SuspiciousMethodCalls
                    if (!clusterAddressList.contains(address)) {
                        clusterConnectionManager.disconnectPoint(address);
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

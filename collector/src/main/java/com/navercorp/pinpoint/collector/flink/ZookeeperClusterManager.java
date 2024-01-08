/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.flink;

import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.collector.util.AddressParser;
import com.navercorp.pinpoint.collector.util.MultipleAddress;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.ConnectionException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonState;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonStateContext;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GetAndRegisterTask getAndRegisterTask = new GetAndRegisterTask();
    private final StopTask stopTask = new StopTask();

    private final CommonStateContext workerState = new CommonStateContext();

    private final ZookeeperClient client;
    private final ClusterConnectionManager clusterConnectionManager;
    private final String parentPath;

    private final AtomicBoolean retryMode = new AtomicBoolean(false);

    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>(1);

    private final Thread workerThread;

    // private final Timer timer;

    // Register Worker + Job
    // synchronize current status with Zookeeper when an event(job) is triggered.
    // (the number of events does not matter as long as a single event is triggered - subsequent events may be ignored)
    public ZookeeperClusterManager(ZookeeperClient client, String zookeeperClusterPath, ClusterConnectionManager clusterConnectionManager) {
        this.client = Objects.requireNonNull(client, "client");
        Objects.requireNonNull(zookeeperClusterPath, "zookeeperClusterPath");
        this.clusterConnectionManager = Objects.requireNonNull(clusterConnectionManager, "clusterConnectionManager");

        if (!zookeeperClusterPath.endsWith("/")) {
            this.parentPath = zookeeperClusterPath + ZookeeperConstants.PATH_SEPARATOR;
        } else {
            this.parentPath = zookeeperClusterPath;
        }

        final ThreadFactory threadFactory = new PinpointThreadFactory(this.getClass().getSimpleName(), true);
        this.workerThread = threadFactory.newThread(new Worker());
    }

    public void start() {
        switch (this.workerState.getCurrentState()) {
            case NEW -> {
                if (this.workerState.changeStateInitializing()) {
                    logger.info("{} initialization started.", this.getClass().getSimpleName());
                    this.workerThread.start();

                    workerState.changeStateStarted();
                    logger.info("{} initialization completed.", this.getClass().getSimpleName());
                }
            }
            case INITIALIZING ->
                logger.info("{} already initializing.", this.getClass().getSimpleName());
            case STARTED ->
                logger.info("{} already started.", this.getClass().getSimpleName());
            case DESTROYING ->
                throw new IllegalStateException("Already destroying.");
            case STOPPED ->
                throw new IllegalStateException("Already stopped.");
            case ILLEGAL_STATE ->
                throw new IllegalStateException("Invalid State.");
        }
    }

    public void stop() {
        if (!(this.workerState.changeStateDestroying())) {
            CommonState state = this.workerState.getCurrentState();

            logger.info("{} already {}.", this.getClass().getSimpleName(), state);
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

        while (this.workerThread.isAlive()) {
            this.workerThread.interrupt();
            try {
                this.workerThread.join(3000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        this.workerState.changeStateStopped();
        logger.info("{} destroying completed.", this.getClass().getSimpleName());
    }

    public void handleAndRegisterWatcher(String path) {
        if (workerState.isStarted()) {
            if (parentPath.equals(path) || parentPath.equals(path + ZookeeperConstants.PATH_SEPARATOR)) {
                final boolean offerSuccess = queue.offer(getAndRegisterTask);
                if (!offerSuccess) {
                    logger.info("Message Queue is Full.");
                }
            } else {
                logger.info("Invalid Path {}.", path);
            }
        } else {
            CommonState state = this.workerState.getCurrentState();
            logger.info("{} invalid state {}.", this.getClass().getSimpleName(), state);
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
                } else if (task instanceof GetAndRegisterTask registerTask) {
                    boolean success = registerTask.handleAndRegisterWatcher0();
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

    class GetAndRegisterTask implements Task {

        private boolean handleAndRegisterWatcher0() {
            boolean needNotRetry = false;
            try {
                client.createPath(parentPath);

                List<Address> targetAddressList = getTargetAddressList(parentPath);
                List<Address> connectedAddressList = clusterConnectionManager.getConnectedAddressList();

                logger.info("Handle register and remove Task. Current Address List = {}, Cluster Address List = {}", connectedAddressList, targetAddressList);

                for (Address targetAddress : targetAddressList) {
                    if (!connectedAddressList.contains(targetAddress)) {
                        clusterConnectionManager.connectPointIfAbsent(targetAddress);
                    }
                }

                for (Address connectedAddress : connectedAddressList) {
                    if (!targetAddressList.contains(connectedAddress)) {
                        clusterConnectionManager.disconnectPoint(connectedAddress);
                    }
                }

                needNotRetry = true;
                return true;
            } catch (Exception e) {
                if (!(e instanceof ConnectionException)) {
                    needNotRetry = true;
                }
            }

            return needNotRetry;
        }

        private List<Address> getTargetAddressList(String parentPath) throws PinpointZookeeperException {
            List<Address> result = new ArrayList<>();

            List<String> childNodeList = client.getChildNodeList(parentPath, true);

            try {
                for (String childNodeName : childNodeList) {
                    String fullPath = ZKPaths.makePath(parentPath, childNodeName);
                    byte[] data = client.getData(fullPath);
                    String nodeContents = BytesUtils.toString(data);

                    String[] nodeAddresses = nodeContents.split("\r\n");

                    Address address = AddressParser.parseAddress(childNodeName);
                    if (nodeAddresses.length > 1) {
                        List<String> hostList = createHostList(nodeAddresses, address.getHost());
                        result.add(new MultipleAddress(hostList, address.getPort()));
                    } else {
                        result.add(address);
                    }
                }
                return result;
            } catch (Exception e) {
                logger.warn("Failed to process getting detail address. message:{}", e.getMessage(), e);
            }

            return AddressParser.parseAddressList(childNodeList);
        }

        private List<String> createHostList(String[] hostAddresses, String representativeHostAddress) {
            List<String> hostAddressList = new ArrayList<>(hostAddresses.length);

            hostAddressList.add(representativeHostAddress);
            for (String hostAddress : hostAddresses) {
                if (hostAddressList.contains(hostAddress)) {
                    continue;
                }
                hostAddressList.add(hostAddress);
            }

            return hostAddressList;
        }

    }

    static class StopTask implements Task {

    }

}

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.cluster.WorkerState;
import com.navercorp.pinpoint.collector.cluster.WorkerStateContext;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.TimeoutException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.job.DeleteJob;
import com.navercorp.pinpoint.collector.cluster.zookeeper.job.Job;
import com.navercorp.pinpoint.collector.cluster.zookeeper.job.UpdateJob;
import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.util.MapUtils;

/**
 * Class should be thread-safe as jobs are only executed in-order inside the class
 * 
 * @author koo.taejin
 */
public class ZookeeperLatestJobWorker implements Runnable {

    private static final Charset charset = Charset.forName("UTF-8");

    private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
    private static final String PINPOINT_COLLECTOR_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/collector";

    private static final String PATH_SEPARATOR = "/";
    private static final String PROFILER_SEPARATOR = "\r\n";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lock = new Object();

    private final WorkerStateContext workerState;
    private final Thread workerThread;

    private final String collectorUniqPath;

    private final ZookeeperClient zookeeperClient;

    private final ConcurrentHashMap<PinpointServer, Job> latestJobRepository = new ConcurrentHashMap<PinpointServer, Job>();

    // Storage for managing PinpointServers received by Worker
    private final CopyOnWriteArrayList<PinpointServer> pinpointServerRepository = new CopyOnWriteArrayList<PinpointServer>();

    private final BlockingQueue<Job> leakJobQueue = new LinkedBlockingQueue<Job>();

    public ZookeeperLatestJobWorker(ZookeeperClient zookeeperClient, String serverIdentifier) {
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

        logger.info("{} destroying started.", this.getClass().getSimpleName());
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

    @Override
    public void run() {

        // Things to consider
        // spinlock possible when events are not deleted
        // may lead to PinpointServer leak when events are left unresolved
        while (workerState.isStarted()) {
            boolean eventCreated = await(60000, 200);
            if (!workerState.isStarted()) {
                break;
            }

            // handle events
            // check and handle PinpointServer leak if events are not triggered
            if (eventCreated) {
                // to avoid ConcurrentModificationException
                Iterator<PinpointServer> keyIterator = getLatestJobRepositoryKeyIterator();

                while (keyIterator.hasNext()) {
                    PinpointServer pinpointServer = keyIterator.next();
                    Job job = getJob(pinpointServer);
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
                // take care of leaked jobs - jobs may leak due to timing mismatch while deleting jobs
                logger.debug("LeakDetector Start.");

                while (true) {
                    Job job = leakJobQueue.poll();
                    if (job == null) {
                        break;
                    }

                    if (job instanceof UpdateJob) {
                        putRetryJob(new UpdateJob(job.getPinpointServer(), 1, ((UpdateJob) job).getContents()));
                    }
                }

                for (PinpointServer pinpointServer : pinpointServerRepository) {
                    if (SocketStateCode.isClosed(pinpointServer.getCurrentStateCode())) {
                        logger.info("LeakDetector Find Leak PinpointServer={}.", pinpointServer);
                        putJob(new DeleteJob(pinpointServer));
                    }
                }

            }
        }

        logger.info("{} stopped", this.getClass().getSimpleName());
    }

    public boolean handleUpdate(UpdateJob job) {
        PinpointServer pinpointServer = job.getPinpointServer();

        SocketStateCode code = pinpointServer.getCurrentStateCode();
        if (SocketStateCode.isClosed(code)) {
            putJob(new DeleteJob(pinpointServer));
            return false;
        }

        try {
            String addContents = createProfilerContents(pinpointServer);

            if (zookeeperClient.exists(collectorUniqPath)) {
                byte[] contents = zookeeperClient.getData(collectorUniqPath);

                String data = addIfAbsentContents(new String(contents, charset), addContents);
                zookeeperClient.setData(collectorUniqPath, data.getBytes(charset));
            } else {
                zookeeperClient.createPath(collectorUniqPath);

                // should return error even if NODE exists if the data is important
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
        PinpointServer pinpointServer = job.getPinpointServer();

        try {
            if (zookeeperClient.exists(collectorUniqPath)) {
                byte[] contents = zookeeperClient.getData(collectorUniqPath);

                String removeContents = createProfilerContents(pinpointServer);
                String data = removeIfExistContents(new String(contents, charset), removeContents);

                zookeeperClient.setData(collectorUniqPath, data.getBytes(charset));
            }
            pinpointServerRepository.remove(pinpointServer);
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

    public List<PinpointServer> getRegisteredPinpointServerList() {
        return new ArrayList<PinpointServer>(pinpointServerRepository);
    }

    /**
     * Waits for events to trigger for a given time.
     *
     * @param waitTimeMillis total time to wait for events to trigger in milliseconds
     * @param waitUnitTimeMillis time to wait for each wait attempt in milliseconds
     * @return true if event triggered, false otherwise
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
                } catch (InterruptedException ignore) {
//                    Thread.currentThread().interrupt();
//                    TODO check Interrupted state
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

    private Iterator<PinpointServer> getLatestJobRepositoryKeyIterator() {
        synchronized (lock) {
            return latestJobRepository.keySet().iterator();
        }
    }

    // must be invoked within a Runnable only
    private Job getJob(PinpointServer pinpointServer) {
        synchronized (lock) {
            Job job = latestJobRepository.remove(pinpointServer);
            return job;
        }
    }

    public void putJob(Job job) {
        PinpointServer pinpointServer = job.getPinpointServer();
        if (!checkRequiredProperties(pinpointServer)) {
            return;
        }

        synchronized (lock) {
            pinpointServerRepository.addIfAbsent(pinpointServer);
            latestJobRepository.put(pinpointServer, job);
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

        PinpointServer pinpointServer = job.getPinpointServer();

        synchronized (lock) {
            latestJobRepository.putIfAbsent(pinpointServer, job);
            lock.notifyAll();
        }
    }

    private String bindingPathAndZnode(String path, String znodeName) {
        StringBuilder fullPath = new StringBuilder();

        fullPath.append(path);
        if (!path.endsWith(PATH_SEPARATOR)) {
            fullPath.append(PATH_SEPARATOR);
        }
        fullPath.append(znodeName);

        return fullPath.toString();
    }

    private boolean checkRequiredProperties(PinpointServer pinpointServer) {
        Map<Object, Object> agentProperties = pinpointServer.getChannelProperties();
        final String applicationName = MapUtils.getString(agentProperties, AgentHandshakePropertyType.APPLICATION_NAME.getName());
        final String agentId = MapUtils.getString(agentProperties, AgentHandshakePropertyType.AGENT_ID.getName());
        final Long startTimeStamp = MapUtils.getLong(agentProperties, AgentHandshakePropertyType.START_TIMESTAMP.getName());

        if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId) || startTimeStamp == null || startTimeStamp <= 0) {
            logger.warn("ApplicationName({}) and AgentId({}) and startTimeStamp({}) may not be null.", applicationName, agentId);
            return false;
        }

        return true;
    }

    private String createProfilerContents(PinpointServer pinpointServer) {
        StringBuilder profilerContents = new StringBuilder();

        Map<Object, Object> agentProperties = pinpointServer.getChannelProperties();
        final String applicationName = MapUtils.getString(agentProperties, AgentHandshakePropertyType.APPLICATION_NAME.getName());
        final String agentId = MapUtils.getString(agentProperties, AgentHandshakePropertyType.AGENT_ID.getName());
        final Long startTimeStamp = MapUtils.getLong(agentProperties, AgentHandshakePropertyType.START_TIMESTAMP.getName());

        if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId) || startTimeStamp == null || startTimeStamp <= 0) {
            logger.warn("ApplicationName({}) and AgentId({}) and startTimeStamp({}) may not be null.", applicationName, agentId);
            return StringUtils.EMPTY;
        }

        profilerContents.append(applicationName);
        profilerContents.append(":");
        profilerContents.append(agentId);
        profilerContents.append(":");
        profilerContents.append(startTimeStamp);

        return profilerContents.toString();
    }

    private String addIfAbsentContents(String contents, String addContents) {
        String[] allContents = contents.split(PROFILER_SEPARATOR);

        for (String eachContent : allContents) {
            if (StringUtils.equals(eachContent.trim(), addContents.trim())) {
                return contents;
            }
        }

        return contents + PROFILER_SEPARATOR + addContents;
    }

    private String removeIfExistContents(String contents, String removeContents) {
        StringBuilder newContents = new StringBuilder(contents.length());

        String[] allContents = contents.split(PROFILER_SEPARATOR);

        Iterator<String> stringIterator = Arrays.asList(allContents).iterator();

        while (stringIterator.hasNext()) {
            String eachContent = stringIterator.next();

            if (StringUtils.isBlank(eachContent)) {
                continue;
            }

            if (!StringUtils.equals(eachContent.trim(), removeContents.trim())) {
                newContents.append(eachContent);

                if (stringIterator.hasNext()) {
                    newContents.append(PROFILER_SEPARATOR);
                }
            }
        }

        return newContents.toString();
    }

}

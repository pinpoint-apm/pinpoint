/*
 * Copyright 2016 NAVER Corp.
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

import com.navercorp.pinpoint.collector.cluster.zookeeper.job.ZookeeperJob;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonStateContext;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * @Author Taejin Koo
 */
public class ZookeeperJobWorker implements Runnable {

    private static final Charset charset = Charset.forName("UTF-8");

    private static final String PINPOINT_CLUSTER_PATH = "/pinpoint-cluster";
    private static final String PINPOINT_COLLECTOR_CLUSTER_PATH = PINPOINT_CLUSTER_PATH + "/collector";

    private static final String PATH_SEPARATOR = "/";
    private static final String PROFILER_SEPARATOR = "\r\n";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object lock = new Object();

    private final CommonStateContext workerState;
    private final String collectorUniqPath;
    private final ZookeeperClient zookeeperClient;
    private final PinpointServerRepository pinpointServerRepository = new PinpointServerRepository();
    private final BlockingQueue<ZookeeperJob> jobQueue = new LinkedBlockingQueue<>();
    private Thread workerThread;

    public ZookeeperJobWorker(ZookeeperClient zookeeperClient, String serverIdentifier) {
        this.zookeeperClient = zookeeperClient;

        this.workerState = new CommonStateContext();

        this.collectorUniqPath = bindingPathAndZNode(PINPOINT_COLLECTOR_CLUSTER_PATH, serverIdentifier);
    }

    public void start() {
        final ThreadFactory threadFactory = new PinpointThreadFactory(this.getClass().getSimpleName(), true);
        this.workerThread = threadFactory.newThread(this);

        switch (this.workerState.getCurrentState()) {
            case NEW:
                if (this.workerState.changeStateInitializing()) {
                    logger.info("start() started.");
                    workerState.changeStateStarted();

                    this.workerThread.start();
                    logger.info("start() completed.");

                    break;
                }
            case INITIALIZING:
                logger.info("start() failed. cause: already initializing.");
                break;
            case STARTED:
                logger.info("start() failed. cause: already initializing.");
                break;
            case DESTROYING:
                throw new IllegalStateException("Already destroying.");
            case STOPPED:
                throw new IllegalStateException(ClassUtils.simpleClassName(this) + " start() failed. caused:Already stopped.");
            case ILLEGAL_STATE:
                throw new IllegalStateException(ClassUtils.simpleClassName(this) + " start() failed. caused:Invalid State.");
        }
    }

    public void stop() {
        if (!(this.workerState.changeStateDestroying())) {
            logger.info("stop() failed. caused:Unexpected State.");
            return;
        }

        logger.info("stop() started.");
        boolean interrupted = false;
        while (workerThread != null && this.workerThread.isAlive()) {
            this.workerThread.interrupt();
            try {
                this.workerThread.join(100L);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        this.workerState.changeStateStopped();
        logger.info("stop() completed.");
    }

    private String bindingPathAndZNode(String path, String zNodeName) {
        StringBuilder fullPath = new StringBuilder(StringUtils.length(path) + StringUtils.length(zNodeName) + 1);

        fullPath.append(path);
        if (!path.endsWith(PATH_SEPARATOR)) {
            fullPath.append(PATH_SEPARATOR);
        }
        fullPath.append(zNodeName);

        return fullPath.toString();
    }

    public void addPinpointServer(PinpointServer pinpointServer) {
        String key = getKey(pinpointServer);
        synchronized (lock) {
            boolean keyCreated = pinpointServerRepository.addAndIsKeyCreated(key, pinpointServer);
            if (keyCreated) {
                putZookeeperJob(new ZookeeperJob(ZookeeperJob.Type.ADD, key));
            }
        }
    }

    public byte[] getClusterData() {
        try {
            return zookeeperClient.getData(collectorUniqPath);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }

    public void removePinpointServer(PinpointServer pinpointServer) {
        String key = getKey(pinpointServer);
        synchronized (lock) {
            boolean keyRemoved = pinpointServerRepository.removeAndGetIsKeyRemoved(key, pinpointServer);
            if (keyRemoved) {
                putZookeeperJob(new ZookeeperJob(ZookeeperJob.Type.REMOVE, key));
            }
        }
    }

    public void clear() {
        synchronized (lock) {
            pinpointServerRepository.clear();
            jobQueue.clear();
            putZookeeperJob(new ZookeeperJob(ZookeeperJob.Type.CLEAR));
        }
    }

    private boolean putZookeeperJob(ZookeeperJob zookeeperJob) {
        synchronized (lock) {
            try {
                jobQueue.put(zookeeperJob);
                lock.notifyAll();
                return true;
            } catch (InterruptedException ignore) {
            }
        }
        return false;
    }

    @Override
    public void run() {
        logger.info("run() started.");

        ZookeeperJob latestHeadJob = null;

        // Things to consider
        // spinLock possible when events are not deleted
        // may lead to PinpointServer leak when events are left unresolved
        while (workerState.isStarted()) {
            boolean eventExists = awaitJob(60000, 200);
            if (eventExists) {
                ZookeeperJob headJob = jobQueue.peek();

                if (latestHeadJob != null && latestHeadJob == headJob) {
                    // for defence spinLock.
                    await(1000);
                }

                if (headJob != null) {
                    latestHeadJob = headJob;
                    boolean completed = handle(headJob);
                    if (completed) {
                        jobQueue.remove(headJob);
                    }
                }
            }
        }

        logger.info("run() completed.");
    }

    /**
     * Waits for events to trigger for a given time.
     *
     * @param waitTimeMillis total time to wait for events to trigger in milliseconds
     * @param waitUnitTimeMillis time to wait for each wait attempt in milliseconds
     * @return true if event triggered, false otherwise
     */
    private boolean awaitJob(long waitTimeMillis, long waitUnitTimeMillis) {
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

            while (jobQueue.isEmpty() && !isOverWaitTime(waitTime, startTimeMillis) && workerState.isStarted()) {
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

    private void await(long waitTimeMillis) {
        try {
            Thread.sleep(waitTimeMillis);
        } catch (InterruptedException e) {
        }
    }

    private boolean isOverWaitTime(long waitTimeMillis, long startTimeMillis) {
        return waitTimeMillis < (System.currentTimeMillis() - startTimeMillis);
    }

    private boolean handle(ZookeeperJob job) {
        ZookeeperJob.Type type = job.getType();

        switch (type) {
            case ADD:
                return handleUpdate(job);
            case REMOVE:
                return handleDelete(job);
            case CLEAR:
                return handleClear(job);
        }

        return false;
    }

    private boolean handleUpdate(ZookeeperJob job) {
        String addContents = job.getKey();
        try {
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
        }
        return false;
    }

    private boolean handleDelete(ZookeeperJob job) {
        String removeContents = job.getKey();
        try {
            if (zookeeperClient.exists(collectorUniqPath)) {
                byte[] contents = zookeeperClient.getData(collectorUniqPath);

                String data = removeIfExistContents(new String(contents, charset), removeContents);

                zookeeperClient.setData(collectorUniqPath, data.getBytes(charset));
            }
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
    }

    private boolean handleClear(ZookeeperJob job) {
        String initContents = job.getKey();
        try {
            if (zookeeperClient.exists(collectorUniqPath)) {
                zookeeperClient.setData(collectorUniqPath, initContents.getBytes(charset));
            } else {
                zookeeperClient.createPath(collectorUniqPath);

                // should return error even if NODE exists if the data is important
                zookeeperClient.createNode(collectorUniqPath, initContents.getBytes(charset));
            }
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
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

    private String getKey(PinpointServer pinpointServer) {
        Map<Object, Object> properties = pinpointServer.getChannelProperties();
        final String applicationName = MapUtils.getString(properties, HandshakePropertyType.APPLICATION_NAME.getName());
        final String agentId = MapUtils.getString(properties, HandshakePropertyType.AGENT_ID.getName());
        final Long startTimeStamp = MapUtils.getLong(properties, HandshakePropertyType.START_TIMESTAMP.getName());

        if (StringUtils.isBlank(applicationName) || StringUtils.isBlank(agentId) || startTimeStamp == null || startTimeStamp <= 0) {
            return StringUtils.EMPTY;
        }

        return applicationName + ":" + agentId + ":" + startTimeStamp;
    }

}

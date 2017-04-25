/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadFactory;

/**
 * @author Taejin Koo
 */
public class ZookeeperJobWorker implements Runnable {

    private static final Charset charset = StandardCharsets.UTF_8;

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
    private final ConcurrentLinkedDeque<ZookeeperJob> zookeeperJobDeque = new ConcurrentLinkedDeque<>();
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
        if (logger.isDebugEnabled()) {
            logger.debug("addPinpointServer server:{}, properties:{}", pinpointServer, pinpointServer.getChannelProperties());
        }

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
        if (logger.isDebugEnabled()) {
            logger.debug("removePinpointServer server:{}, properties:{}", pinpointServer, pinpointServer.getChannelProperties());
        }

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
            zookeeperJobDeque.clear();
            putZookeeperJob(new ZookeeperJob(ZookeeperJob.Type.CLEAR));
        }
    }

    private boolean putZookeeperJob(ZookeeperJob zookeeperJob) {
        synchronized (lock) {
            boolean added = zookeeperJobDeque.add(zookeeperJob);
            lock.notifyAll();
            return added;
        }
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
                List<ZookeeperJob> zookeeperJobList = getLatestZookeeperJobList();
                if (CollectionUtils.isEmpty(zookeeperJobList)) {
                    continue;
                }


                ZookeeperJob headJob = ListUtils.getFirst(zookeeperJobList);
                if (latestHeadJob != null && latestHeadJob == headJob) {
                    // for defence spinLock (zookeeper problem, etc..)
                    await(500);
                }

                latestHeadJob = headJob;
                boolean completed = handle(zookeeperJobList);
                if (!completed) {
                    // rollback
                    for (int i = zookeeperJobList.size() - 1; i >= 0; i--) {
                        zookeeperJobDeque.addFirst(zookeeperJobList.get(i));
                    }
                }
            }
        }

        logger.info("run() completed.");
    }

    private List<ZookeeperJob> getLatestZookeeperJobList() {
        ZookeeperJob defaultJob = zookeeperJobDeque.poll();
        if (defaultJob == null) {
            return Collections.emptyList();
        }

        List<ZookeeperJob> result = new ArrayList<>();
        result.add(defaultJob);

        while (true) {
            ZookeeperJob zookeeperJob = zookeeperJobDeque.poll();
            if (zookeeperJob == null) {
                break;
            }
            if (zookeeperJob.getType() != defaultJob.getType()) {
                zookeeperJobDeque.addFirst(zookeeperJob);
                break;
            }
            result.add(zookeeperJob);
        }

        return result;
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

            while (zookeeperJobDeque.isEmpty() && !isOverWaitTime(waitTime, startTimeMillis) && workerState.isStarted()) {
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

    private boolean handle(List<ZookeeperJob> zookeeperJobList) {
        if (CollectionUtils.isEmpty(zookeeperJobList)) {
            logger.warn("zookeeperJobList may not be empty");
            return false;
        }

        ZookeeperJob defaultJob = ListUtils.getFirst(zookeeperJobList);
        ZookeeperJob.Type type = defaultJob.getType();
        switch (type) {
            case ADD:
                return handleUpdate(zookeeperJobList);
            case REMOVE:
                return handleDelete(zookeeperJobList);
            case CLEAR:
                return handleClear(zookeeperJobList);
        }

        return false;
    }

    private boolean handleUpdate(List<ZookeeperJob> zookeeperJobList) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleUpdate zookeeperJobList:{}", zookeeperJobList);
        }

        List<String> addContentCandidateList = new ArrayList<>(zookeeperJobList.size());
        for (ZookeeperJob zookeeperJob : zookeeperJobList) {
            addContentCandidateList.add(zookeeperJob.getKey());
        }

        try {
            if (zookeeperClient.exists(collectorUniqPath)) {
                byte[] contents = zookeeperClient.getData(collectorUniqPath);

                String data = addIfAbsentContents(new String(contents, charset), addContentCandidateList);
                zookeeperClient.setData(collectorUniqPath, data.getBytes(charset));
            } else {
                zookeeperClient.createPath(collectorUniqPath);

                // should return error even if NODE exists if the data is important
                String data = addIfAbsentContents("", addContentCandidateList);
                zookeeperClient.createNode(collectorUniqPath, data.getBytes(charset));
            }
            return true;
        } catch (Exception e) {
            logger.warn("handleUpdate failed. caused:{}, jobSize:{}", e.getMessage(), zookeeperJobList.size(), e);
        }
        return false;
    }

    private boolean handleDelete(List<ZookeeperJob> zookeeperJobList) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleDelete zookeeperJobList:{}", zookeeperJobList);
        }

        List<String> removeContentCandidateList = new ArrayList<>(zookeeperJobList.size());
        for (ZookeeperJob zookeeperJob : zookeeperJobList) {
            removeContentCandidateList.add(zookeeperJob.getKey());
        }

        try {
            if (zookeeperClient.exists(collectorUniqPath)) {
                byte[] contents = zookeeperClient.getData(collectorUniqPath);
                String data = removeIfExistContents(new String(contents, charset), removeContentCandidateList);
                zookeeperClient.setData(collectorUniqPath, data.getBytes(charset));
            }
            return true;
        } catch (Exception e) {
            logger.warn("handleDelete failed. caused:{}, jobSize:{}", e.getMessage(), zookeeperJobList.size(), e);
        }
        return false;
    }

    private boolean handleClear(List<ZookeeperJob> zookeeperJobList) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleClear zookeeperJobList:{}", zookeeperJobList);
        }

        try {
            if (zookeeperClient.exists(collectorUniqPath)) {
                zookeeperClient.setData(collectorUniqPath, "".getBytes(charset));
            } else {
                zookeeperClient.createPath(collectorUniqPath);

                // should return error even if NODE exists if the data is important
                zookeeperClient.createNode(collectorUniqPath, "".getBytes(charset));
            }
            return true;
        } catch (Exception e) {
            logger.warn("handleClear failed. caused:{}, jobSize:{}", e.getMessage(), zookeeperJobList.size(), e);
        }
        return false;
    }

    private String addIfAbsentContents(String originalContent, List<String> addContentCandidateList) {
        List<String> splittedOriginalContent = com.navercorp.pinpoint.common.util.StringUtils.tokenizeToStringList(originalContent, PROFILER_SEPARATOR);

        List<String> addContentList = new ArrayList<>(addContentCandidateList.size());
        for (String addContentCandidate : addContentCandidateList) {
            if (StringUtils.isEmpty(addContentCandidate)) {
                continue;
            }

            boolean exist = isExist(splittedOriginalContent, addContentCandidate);
            if (!exist) {
                addContentList.add(addContentCandidate);
            }
        }

        if (addContentList.isEmpty()) {
            return originalContent;
        }

        StringBuilder newContent = new StringBuilder(originalContent);
        for (String addContent : addContentList) {
            newContent.append(PROFILER_SEPARATOR);
            newContent.append(addContent);
        }
        return newContent.toString();
    }

    private boolean isExist(String[] contents, String value) {
        if (contents == null) {
            return false;
        }

        return isExist(Arrays.asList(contents), value);
    }

    private boolean isExist(List<String> contentList, String value) {
        for (String eachContent : contentList) {
            if (StringUtils.equals(eachContent.trim(), value.trim())) {
                return true;
            }
        }

        return false;
    }

    private String removeIfExistContents(String originalContent, List<String> removeContentCandidateList) {
        StringBuilder newContent = new StringBuilder(originalContent.length());

        List<String> splittedOriginalContent = com.navercorp.pinpoint.common.util.StringUtils.tokenizeToStringList(originalContent, PROFILER_SEPARATOR);
        Iterator<String> originalContentIterator = splittedOriginalContent.iterator();
        while (originalContentIterator.hasNext()) {
            String eachContent = originalContentIterator.next();
            if (StringUtils.isBlank(eachContent)) {
                continue;
            }

            if (!isExist(removeContentCandidateList, eachContent)) {
                newContent.append(eachContent);

                if (originalContentIterator.hasNext()) {
                    newContent.append(PROFILER_SEPARATOR);
                }
            }
        }

        return newContent.toString();
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

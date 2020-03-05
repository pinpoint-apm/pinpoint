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

package com.navercorp.pinpoint.collector.cluster.zookeeper;

import com.navercorp.pinpoint.collector.cluster.zookeeper.job.ZookeeperJob;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonStateContext;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadFactory;

/**
 * @author Taejin Koo
 */
public class ZookeeperJobWorker implements Runnable {

    private static final String PROFILER_SEPARATOR = "\r\n";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final byte[] EMPTY_DATA_BYTES = BytesUtils.toBytes("");

    private final Object lock = new Object();

    private final CommonStateContext workerState;
    private final String collectorUniqPath;
    private final ZookeeperClient zookeeperClient;
    private final ConcurrentLinkedDeque<ZookeeperJob> zookeeperJobDeque = new ConcurrentLinkedDeque<>();
    private Thread workerThread;

    public ZookeeperJobWorker(ZookeeperClient zookeeperClient, String serverIdentifier) {
        this.zookeeperClient = zookeeperClient;

        this.workerState = new CommonStateContext();

        this.collectorUniqPath = ZKPaths.makePath(ZookeeperConstants.PINPOINT_COLLECTOR_CLUSTER_PATH, serverIdentifier);
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

    public void addPinpointServer(String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("addPinpointServer key:{}", key);
        }

        synchronized (lock) {
            putZookeeperJob(new ZookeeperJob(ZookeeperJob.Type.ADD, key));
        }
    }

    public List<String> getClusterList() {
        try {
            final String clusterData = getClusterData();
            return tokenize(clusterData);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private String getClusterData() throws PinpointZookeeperException, InterruptedException {
        try {
            final byte[] result = zookeeperClient.getData(collectorUniqPath);
            if (result == null) {
                return StringUtils.EMPTY;
            }
            return BytesUtils.toString(result);
        } catch (Exception e) {
            logger.warn("getClusterData failed. message:{}", e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    public void removePinpointServer(String key) {
        if (logger.isDebugEnabled()) {
            logger.debug("removePinpointServer key:{}", key);
        }

        synchronized (lock) {
            putZookeeperJob(new ZookeeperJob(ZookeeperJob.Type.REMOVE, key));
        }
    }

    public void clear() {
        synchronized (lock) {
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

        final List<String> addContentCandidateList = getZookeeperKeyList(zookeeperJobList);

        try {
            zookeeperClient.createPath(collectorUniqPath);

            String currentData = getClusterData();
            final String newData = addIfAbsentContents(currentData, addContentCandidateList);

            CreateNodeMessage createNodeMessage = new CreateNodeMessage(collectorUniqPath, BytesUtils.toBytes(newData), true);
            zookeeperClient.createOrSetNode(createNodeMessage);
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

        final List<String> removeContentCandidateList = getZookeeperKeyList(zookeeperJobList);

        try {
            zookeeperClient.createPath(collectorUniqPath);

            final String currentData = getClusterData();
            final String newData = removeIfExistContents(currentData, removeContentCandidateList);

            CreateNodeMessage createNodeMessage = new CreateNodeMessage(collectorUniqPath, BytesUtils.toBytes(newData), true);
            zookeeperClient.createOrSetNode(createNodeMessage);
            return true;
        } catch (Exception e) {
            logger.warn("handleDelete failed. caused:{}, jobSize:{}", e.getMessage(), zookeeperJobList.size(), e);
        }
        return false;
    }

    private List<String> getZookeeperKeyList(List<ZookeeperJob> zookeeperJobList) {
        if (zookeeperJobList == null) {
            return Collections.emptyList();
        }

        final List<String> keyList = new ArrayList<>(zookeeperJobList.size());
        for (ZookeeperJob zookeeperJob : zookeeperJobList) {
            keyList.add(zookeeperJob.getKey());
        }
        return keyList;
    }

    private boolean handleClear(List<ZookeeperJob> zookeeperJobList) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleClear zookeeperJobList:{}", zookeeperJobList);
        }

        try {
            CreateNodeMessage createNodeMessage = new CreateNodeMessage(collectorUniqPath, EMPTY_DATA_BYTES, true);
            zookeeperClient.createOrSetNode(createNodeMessage);
            return true;
        } catch (Exception e) {
            logger.warn("handleClear failed. caused:{}, jobSize:{}", e.getMessage(), zookeeperJobList.size(), e);
        }
        return false;
    }

    private String addIfAbsentContents(String clusterDataString, List<String> addContentCandidateList) {
        final List<String> clusterDataList = tokenize(clusterDataString);

        final List<String> addContentList = getChangeList(clusterDataList, addContentCandidateList);

        if (addContentList.isEmpty()) {
            return clusterDataString;
        }

        return join(clusterDataString, addContentList);
    }

    private String join(String originalContent, List<String> addContentList) {
        final StringBuilder newContent = new StringBuilder(originalContent);
        for (String addContent : addContentList) {
            newContent.append(PROFILER_SEPARATOR);
            newContent.append(addContent);
        }
        return newContent.toString();
    }

    private boolean isExist(List<String> contentList, String value) {
        for (String eachContent : contentList) {
            if (StringUtils.equals(eachContent.trim(), value.trim())) {
                return true;
            }
        }

        return false;
    }

    private String removeIfExistContents(String clusterDataString, List<String> removeClusterDataList) {

        final List<String> clusterDataList = tokenize(clusterDataString);

        final List<String> remainCluster = getChangeList(removeClusterDataList, clusterDataList);

        return StringUtils.join(remainCluster, PROFILER_SEPARATOR);
    }

    private List<String> getChangeList(List<String> originalList, List<String> changeList) {
        final List<String> result = new ArrayList<>(changeList.size());
        for (String eachContent : changeList) {
            final boolean exist = isExist(originalList, eachContent);
            if (!exist) {
                result.add(eachContent);
            }
        }
        return result;
    }

    private List<String> tokenize(String str) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }

        final String[] tokenArray = org.springframework.util.StringUtils.tokenizeToStringArray(str, PROFILER_SEPARATOR);
        return Arrays.asList(tokenArray);
    }

}

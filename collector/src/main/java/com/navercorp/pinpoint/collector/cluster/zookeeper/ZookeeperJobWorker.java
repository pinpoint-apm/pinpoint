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
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonStateContext;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.rpc.util.ClassUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class ZookeeperJobWorker<K> implements Runnable, ClusterJobWorker<K> {

    private static final String PROFILER_SEPARATOR = "\r\n";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final byte[] EMPTY_DATA_BYTES = BytesUtils.toBytes("");

    private final Object lock = new Object();

    private final CommonStateContext workerState;
    private final String collectorUniqPath;
    private final ZookeeperClient zookeeperClient;
    private final LinkedBlockingDeque<ZookeeperJob<K>> jobDeque = new LinkedBlockingDeque<>();
    private Thread workerThread;

    public ZookeeperJobWorker(ZookeeperClient zookeeperClient, String connectedAgentZNodePath) {
        this.zookeeperClient = zookeeperClient;

        this.workerState = new CommonStateContext();

        this.collectorUniqPath = Objects.requireNonNull(connectedAgentZNodePath, "connectedAgentZNodePath");
    }

    @Override
    public void start() {
        logger.info("start() collectorUniqPath:{}", collectorUniqPath);

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

    @Override
    public void stop() {
        if (!(this.workerState.changeStateDestroying())) {
            logger.info("stop() failed. caused:Unexpected State.");
            return;
        }

        logger.info("stop() started.");
        while (workerThread != null && this.workerThread.isAlive()) {
            this.workerThread.interrupt();
            try {
                this.workerThread.join(3000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        this.workerState.changeStateStopped();
        logger.info("stop() completed.");
    }

    @Override
    public void addPinpointServer(K key) {
        if (logger.isDebugEnabled()) {
            logger.debug("addPinpointServer key:{}", key);
        }

        ZookeeperJob<K> job = new ZookeeperJob<>(ZookeeperJob.Type.ADD, key);
        synchronized (lock) {
            putZookeeperJob(job);
        }
    }

    @Override
    public List<String> getClusterList() {
        try {
            final String clusterData = getClusterData();
            return tokenize(clusterData);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private String getClusterData() {
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

    @Override
    public void removePinpointServer(K key) {
        if (logger.isDebugEnabled()) {
            logger.debug("removePinpointServer key:{}", key);
        }

        ZookeeperJob<K> job = new ZookeeperJob<>(ZookeeperJob.Type.REMOVE, key);
        synchronized (lock) {
            putZookeeperJob(job);
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            jobDeque.clear();
            putZookeeperJob(new ZookeeperJob<>(ZookeeperJob.Type.CLEAR));
        }
    }

    private boolean putZookeeperJob(ZookeeperJob<K> zookeeperJob) {
        synchronized (lock) {
            return jobDeque.add(zookeeperJob);
        }
    }

    @Override
    public void run() {
        logger.info("run() started.");

        ZookeeperJob<K> latestHeadJob = null;

        // Things to consider
        // spinLock possible when events are not deleted
        // may lead to PinpointServer leak when events are left unresolved
        while (workerState.isStarted()) {
            try {
                List<ZookeeperJob<K>> zookeeperJobList = getLatestZookeeperJobList();
                if (CollectionUtils.isEmpty(zookeeperJobList)) {
                    continue;
                }

                ZookeeperJob<K> headJob = CollectionUtils.firstElement(zookeeperJobList);
                if (latestHeadJob != null && latestHeadJob == headJob) {
                    // for defence spinLock (zookeeper problem, etc..)
                    Thread.sleep(500);
                }
                latestHeadJob = headJob;

                boolean completed = handle(zookeeperJobList);
                if (!completed) {
                    // rollback
                    for (int i = zookeeperJobList.size() - 1; i >= 0; i--) {
                        jobDeque.addFirst(zookeeperJobList.get(i));
                    }
                }
            } catch (InterruptedException e) {
                logger.info("{} thread interrupted", workerThread.getName());
                break;
            }
        }

        logger.info("run() completed.");
    }

    private List<ZookeeperJob<K>> getLatestZookeeperJobList() throws InterruptedException {
        ZookeeperJob<K> defaultJob = jobDeque.poll(3000, TimeUnit.MILLISECONDS);
        if (defaultJob == null) {
            return Collections.emptyList();
        }

        List<ZookeeperJob<K>> result = new ArrayList<>();
        result.add(defaultJob);

        while (true) {
            ZookeeperJob<K> zookeeperJob = jobDeque.poll();
            if (zookeeperJob == null) {
                break;
            }
            if (zookeeperJob.getType() != defaultJob.getType()) {
                jobDeque.addFirst(zookeeperJob);
                break;
            }
            result.add(zookeeperJob);
        }

        return result;
    }

    private boolean handle(List<ZookeeperJob<K>> zookeeperJobList) {
        if (CollectionUtils.isEmpty(zookeeperJobList)) {
            logger.warn("zookeeperJobList may not be empty");
            return false;
        }

        ZookeeperJob<K> defaultJob = CollectionUtils.firstElement(zookeeperJobList);
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

    private boolean handleUpdate(List<ZookeeperJob<K>> zookeeperJobList) {
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

    private boolean handleDelete(List<ZookeeperJob<K>> zookeeperJobList) {
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

    private List<String> getZookeeperKeyList(List<ZookeeperJob<K>> zookeeperJobList) {
        if (zookeeperJobList == null) {
            return Collections.emptyList();
        }

        final List<String> keyList = new ArrayList<>(zookeeperJobList.size());
        for (ZookeeperJob<K> zookeeperJob : zookeeperJobList) {
            keyList.add(zookeeperJob.getKey().toString());
        }
        return keyList;
    }

    private boolean handleClear(List<ZookeeperJob<K>> zookeeperJobList) {
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
        StringJoiner buffer = new StringJoiner(PROFILER_SEPARATOR);
        buffer.add(originalContent);
        for (String content : addContentList) {
            buffer.add(content);
        }
        return buffer.toString();
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

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
import com.navercorp.pinpoint.common.server.cluster.AgentInfoKey;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonStateContext;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class ZookeeperJobWorker2 implements Runnable, ClusterJobWorker<AgentInfoKey> {
    private static final String PROFILER_SEPARATOR = "\r\n";
    public static final String APPLICATION_NAME_SEPARATOR = "$$";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final byte[] EMPTY_DATA_BYTES = BytesUtils.toBytes("");

    private final Object lock = new Object();

    private final CommonStateContext workerState;
    private final String collectorUniqPath;
    private final ZookeeperClient zookeeperClient;
    private final LinkedBlockingDeque<ZookeeperJob<AgentInfoKey>> jobDeque = new LinkedBlockingDeque<>();
    private Thread workerThread;

    public ZookeeperJobWorker2(ZookeeperClient zookeeperClient, String connectedAgentZNodePath) {
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
    public void addPinpointServer(AgentInfoKey key) {
        if (logger.isDebugEnabled()) {
            logger.debug("addPinpointServer key:{}", key);
        }

        ZookeeperJob<AgentInfoKey> job = new ZookeeperJob<>(ZookeeperJob.Type.ADD, key);
        synchronized (lock) {
            putZookeeperJob(job);
        }
    }

    @Override
    public List<String> getClusterList() {
        try {
            List<String> childNodeList = zookeeperClient.getChildNodeList(collectorUniqPath, false);

            return childNodeList;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private String getClusterData(String path) {
        try {
            final byte[] result = zookeeperClient.getData(path);
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
    public void removePinpointServer(AgentInfoKey key) {
        if (logger.isDebugEnabled()) {
            logger.debug("removePinpointServer key:{}", key);
        }

        ZookeeperJob<AgentInfoKey> job = new ZookeeperJob<>(ZookeeperJob.Type.REMOVE, key);
        synchronized (lock) {
            putZookeeperJob(job);
        }
    }

    @Override
    public void clear() {
        ZookeeperJob<AgentInfoKey> job = new ZookeeperJob<>(ZookeeperJob.Type.CLEAR);
        synchronized (lock) {
            jobDeque.clear();
            putZookeeperJob(job);
        }
    }

    private boolean putZookeeperJob(ZookeeperJob<AgentInfoKey> zookeeperJob) {
        synchronized (lock) {
            return jobDeque.add(zookeeperJob);
        }
    }

    @Override
    public void run() {
        logger.info("run() started.");

        // Things to consider
        // spinLock possible when events are not deleted
        // may lead to PinpointServer leak when events are left unresolved
        while (workerState.isStarted()) {
            try {
                ZookeeperJob<AgentInfoKey> job = poll();

                boolean completed = handle(job);
                if (!completed) {
                    logger.warn("cluster job execute fail job:{}", job);
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        logger.info("run() completed.");
    }

    private ZookeeperJob<AgentInfoKey> poll() throws InterruptedException {
        while (true) {
            ZookeeperJob<AgentInfoKey> job = jobDeque.poll(3000, TimeUnit.MILLISECONDS);
            if (job == null) {
                continue;
            }
            return job;
        }
    }

    private boolean handle(ZookeeperJob<AgentInfoKey> job) {

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

    private boolean handleUpdate(ZookeeperJob<AgentInfoKey> job) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleUpdate zookeeperJobList:{}", job);
        }

        final AgentInfoKey key = job.getKey();

        try {
            String path = getPath(key);
            zookeeperClient.createPath(path);

            String currentData = getClusterData(path);
            final String newData = addIfAbsentContents(currentData, key);

            CreateNodeMessage createNodeMessage = new CreateNodeMessage(path, BytesUtils.toBytes(newData), true);
            zookeeperClient.createOrSetNode(createNodeMessage);
            return true;
        } catch (Exception e) {
            logger.warn("handleUpdate failed. caused:{}, jobSize:{}", e.getMessage(), job, e);
        }
        return false;
    }

    private String getPath(AgentInfoKey key) {
        StringJoiner buffer = new StringJoiner(APPLICATION_NAME_SEPARATOR);
        buffer.add(collectorUniqPath);
        buffer.add(key.getApplicationName());
        return buffer.toString();
    }

    private boolean handleDelete(ZookeeperJob<AgentInfoKey> job) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleDelete zookeeperJobList:{}", job);
        }

        final AgentInfoKey key = job.getKey();

        try {
            String path = getPath(key);
            zookeeperClient.createPath(path);

            final String currentData = getClusterData(path);
            final String newData = removeIfExistContents(currentData, key);

            CreateNodeMessage createNodeMessage = new CreateNodeMessage(path, BytesUtils.toBytes(newData), true);
            zookeeperClient.createOrSetNode(createNodeMessage);
            return true;
        } catch (Exception e) {
            logger.warn("handleDelete failed. caused:{}, jobSize:{}", e.getMessage(), job, e);
        }
        return false;
    }


    private boolean handleClear(ZookeeperJob<AgentInfoKey> job) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleClear zookeeperJobList:{}", job);
        }

        try {
            CreateNodeMessage createNodeMessage = new CreateNodeMessage(collectorUniqPath, EMPTY_DATA_BYTES, true);
            zookeeperClient.createNode(createNodeMessage);
            return true;
        } catch (Exception e) {
            logger.warn("handleClear failed. caused:{}, jobSize:{}", e.getMessage(), job, e);
        }
        return false;
    }

    private String addIfAbsentContents(String clusterDataString, AgentInfoKey addContentCandidate) {
        final List<String> clusterDataList = tokenize(clusterDataString);

        List<String> addContentCandidateList = Collections.singletonList(addContentCandidate.toString());
        final List<String> addContentList = getChangeList(clusterDataList, addContentCandidateList);

        if (addContentList.isEmpty()) {
            return clusterDataString;
        }

        return join(clusterDataString, addContentList);
    }

    private String join(String originalContent, List<String> addContentList) {
        StringJoiner joiner = new StringJoiner(PROFILER_SEPARATOR);
        joiner.add(originalContent);
        for (String addContent : addContentList) {
            joiner.add(addContent);
        }
        return joiner.toString();
    }

    private boolean isExist(List<String> contentList, String value) {
        for (String eachContent : contentList) {
            if (StringUtils.equals(eachContent.trim(), value.trim())) {
                return true;
            }
        }

        return false;
    }

    private String removeIfExistContents(String clusterDataString, AgentInfoKey removeClusterData) {

        final List<String> clusterDataList = tokenize(clusterDataString);

        List<String> originalList = Collections.singletonList(removeClusterData.toString());
        final List<String> remainCluster = getChangeList(originalList, clusterDataList);

        return StringUtils.join(remainCluster, PROFILER_SEPARATOR);
    }

    private List<String> getChangeList(List<String> originalList, List<String> changeList) {
        return changeList.stream()
                .filter(eachContent -> !isExist(originalList, eachContent))
                .collect(Collectors.toList());
    }

    private List<String> tokenize(String str) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }

        final String[] tokenArray = org.springframework.util.StringUtils.tokenizeToStringArray(str, PROFILER_SEPARATOR);
        return Arrays.asList(tokenArray);
    }

}

/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.ConnectionException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonStateContext;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class CuratorZookeeperClient implements ZookeeperClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommonStateContext stateContext = new CommonStateContext();

    private final ZookeeperEventWatcher zookeeperEventWatcher;

    private final CuratorZookeeperConnectionManager connectionManager;

    public CuratorZookeeperClient(String hostPort, int sessionTimeout, ZookeeperEventWatcher zookeeperEventWatcher) {
        this.zookeeperEventWatcher = Objects.requireNonNull(zookeeperEventWatcher, "zookeeperEventWatcher");

        this.connectionManager = new CuratorZookeeperConnectionManager(hostPort, sessionTimeout, zookeeperEventWatcher);
    }

    @Override
    public void connect() throws IOException {
        logger.debug("connect() started");
        if (stateContext.changeStateInitializing()) {
            stateContext.changeStateStarted();
            try {
                connectionManager.start();
            } catch (PinpointZookeeperException e) {
                stateContext.changeStateIllegal();
                throw new IOException(e.getMessage(), e);
            }
        } else {
            logger.warn("connect() failed. error : Illegal State. State may be {}.", stateContext.getCurrentState());
        }
    }

    @Override
    public void createPath(String value) throws PinpointZookeeperException {
        checkState();
        String path = getPath(value, false);

        logger.debug("createPath() started. value:{}, path:{}", value, path);

        CuratorFramework client = connectionManager.getZookeeperClient();
        Stat stat = null;
        try {
            stat = client.checkExists().forPath(path);
        } catch (Exception e) {
            ZookeeperExceptionResolver.resolve(e, true);
        }

        if (stat == null) {
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            } catch (KeeperException.NodeExistsException nodeExists) {
                // skip
            } catch (Exception e) {
                ZookeeperExceptionResolver.resolve(e, true);
            }
        }
    }

    private String getPath(String value, boolean includeEndPath) {
        assertPathHasLength(value);

        if (value.length() == 1 && value.charAt(0) == '/') {
            return value;
        }

        if (value.charAt(value.length() - 1) == '/') {
            return value.substring(0, value.length() - 1);
        }

        if (includeEndPath) {
            return value;
        } else {
            ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(value);
            return pathAndNode.getPath();
        }
    }

    @Override
    public void createNode(CreateNodeMessage message) throws PinpointZookeeperException {
        logger.debug("createNode() started. message:{}", message);
        createNode0(message, false);
    }

    @Override
    public void createOrSetNode(CreateNodeMessage message) throws PinpointZookeeperException {
        logger.debug("createOrSetNode() started. message:{}", message);
        createNode0(message, true);
    }

    private void createNode0(CreateNodeMessage message, boolean orSet) throws PinpointZookeeperException {
        checkState();

        try {
            CuratorFramework client = connectionManager.getZookeeperClient();

            CreateBuilder createBuilder = client.create();
            if (message.isCreatingParentPathsIfNeeded()) {
                createBuilder.creatingParentsIfNeeded();
            }
            if (orSet) {
                createBuilder.orSetData();
            }

            String nodePath = message.getNodePath();
            byte[] data = message.getData();
            createBuilder.withMode(CreateMode.EPHEMERAL).forPath(nodePath, data);
        } catch (Exception e) {
            PinpointZookeeperException exception = ZookeeperExceptionResolver.resolve(e);
            throw exception;
        }
    }

    @Override
    public byte[] getData(String path) throws PinpointZookeeperException {
        return getData(path, false);
    }

    @Override
    public byte[] getData(String path, boolean watch) throws PinpointZookeeperException {
        checkState();
        assertPathHasLength(path);

        logger.debug("getData() started. path:{}, watch:{}", path, watch);

        try {
            CuratorFramework client = connectionManager.getZookeeperClient();

            if (watch) {
                byte[] bytes = client.getData().usingWatcher(zookeeperEventWatcher).forPath(path);
                return bytes;
            } else {
                byte[] bytes = client.getData().forPath(path);
                return bytes;
            }
        } catch (Exception e) {
            PinpointZookeeperException resolveException = ZookeeperExceptionResolver.resolve(e);
            throw resolveException;
        }
    }

    @Override
    public List<String> getChildNodeList(String value, boolean watch) throws PinpointZookeeperException, InterruptedException {
        checkState();

        String path = getPath(value, true);

        logger.debug("getChildNodeList() started. path:{}, watch:{}", path, watch);

        try {
            CuratorFramework client = connectionManager.getZookeeperClient();

            if (watch) {
                List<String> childList = client.getChildren().usingWatcher(zookeeperEventWatcher).forPath(path);
                return childList;
            } else {
                List<String> childList = client.getChildren().forPath(path);
                return childList;
            }
        } catch (KeeperException.NoNodeException noNode) {
            // skip
        } catch (Exception e) {
            ZookeeperExceptionResolver.resolve(e, true);
        }

        return Collections.emptyList();
    }

    @Override
    public void delete(String path) throws PinpointZookeeperException {
        checkState();
        assertPathHasLength(path);

        logger.debug("delete() started. path:{}", path);

        try {
            CuratorFramework client = connectionManager.getZookeeperClient();

            client.delete().forPath(path);
        } catch (KeeperException.NoNodeException noNode) {
            // skip
        } catch (Exception e) {
            ZookeeperExceptionResolver.resolve(e, true);
        }
    }

    @Override
    public void close() {
        logger.debug("close() started.");

        if (stateContext.changeStateDestroying()) {
            connectionManager.stop();
            stateContext.changeStateStopped();
        }
    }

    private void checkState() throws PinpointZookeeperException {
        if (!isConnected()) {
            throw new ConnectionException("Instance must be connected.");
        }
    }

    private void assertPathHasLength(String path) {
        Assert.isTrue(StringUtils.hasLength(path), "path must not be empty");
    }

    public boolean isConnected() {
        if (!connectionManager.isConnected() || !stateContext.isStarted()) {
            return false;
        }

        return true;
    }

}

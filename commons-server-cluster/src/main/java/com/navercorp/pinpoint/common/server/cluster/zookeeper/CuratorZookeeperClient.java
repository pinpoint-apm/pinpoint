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
import com.navercorp.pinpoint.common.server.cluster.zookeeper.util.CommonStateContext;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.utils.ZKPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class CuratorZookeeperClient implements ZookeeperClient {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final CommonStateContext stateContext = new CommonStateContext();

    private final ReconnectCondition reconnectCondition =
            new ReconnectCondition(TimeUnit.MINUTES.toMillis(5), (int) TimeUnit.MINUTES.toSeconds(5));
    private final NotConnectedStatus notConnectedStatus = new NotConnectedStatus();

    private final ZookeeperEventWatcher zookeeperEventWatcher;

    private final CuratorZookeeperConnectionManager connectionManager;

    public CuratorZookeeperClient(String hostPort, int sessionTimeout, ZookeeperEventWatcher zookeeperEventWatcher) {
        this.zookeeperEventWatcher = Objects.requireNonNull(zookeeperEventWatcher, "zookeeperEventWatcher");

        this.connectionManager = new CuratorZookeeperConnectionManager(hostPort, sessionTimeout, zookeeperEventWatcher);
    }

    @Override
    public void connect() throws PinpointZookeeperException {
        logger.debug("connect() started");
        if (stateContext.changeStateInitializing()) {
            stateContext.changeStateStarted();
            try {
                connectionManager.start();
            } catch (PinpointZookeeperException e) {
                stateContext.changeStateIllegal();
                throw e;
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

        CuratorFramework curator = connectionManager.getCuratorFramework();
        Stat stat = null;
        try {
            stat = curator.checkExists().forPath(path);
        } catch (Exception e) {
            ZookeeperExceptionResolver.resolveAndThrow(e);
        }

        if (stat == null) {
            try {
                curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            } catch (KeeperException.NodeExistsException ignored) {
                // skip
            } catch (Exception e) {
                ZookeeperExceptionResolver.resolveAndThrow(e);
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
            CuratorFramework curator = connectionManager.getCuratorFramework();

            CreateBuilder createBuilder = curator.create();
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
            ZookeeperExceptionResolver.resolveAndThrow(e);
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
            CuratorFramework curator = connectionManager.getCuratorFramework();

            final GetDataBuilder data = curator.getData();
            if (watch) {
                return data.usingWatcher(zookeeperEventWatcher).forPath(path);
            } else {
                return data.forPath(path);
            }
        } catch (Exception e) {
            throw ZookeeperExceptionResolver.resolve(e);
        }
    }

    @Override
    public List<String> getChildNodeList(String value, boolean watch) throws PinpointZookeeperException {
        checkState();

        String path = getPath(value, true);

        logger.debug("getChildNodeList() started. path:{}, watch:{}", path, watch);

        try {
            CuratorFramework curator = connectionManager.getCuratorFramework();

            final GetChildrenBuilder children = curator.getChildren();
            if (watch) {
                return children.usingWatcher(zookeeperEventWatcher).forPath(path);
            } else {
                return children.forPath(path);
            }
        } catch (KeeperException.NoNodeException noNode) {
            logger.debug("NoNodeException path:{} {}", noNode.getPath(), noNode);
        } catch (Exception e) {
            ZookeeperExceptionResolver.resolveAndThrow(e);
        }

        return Collections.emptyList();
    }

    @Override
    public void delete(String path) throws PinpointZookeeperException {
        checkState();
        assertPathHasLength(path);

        logger.debug("delete() started. path:{}", path);

        try {
            CuratorFramework curator = connectionManager.getCuratorFramework();

            curator.delete().forPath(path);
        } catch (KeeperException.NoNodeException ignored) {
            // skip
        } catch (Exception e) {
            ZookeeperExceptionResolver.resolveAndThrow(e);
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
            notConnectedStatus.update();
            if (reconnectCondition.check(notConnectedStatus)) {
                notConnectedStatus.reset();
                try {
                    final org.apache.curator.CuratorZookeeperClient zookeeperClient = connectionManager.getCuratorFramework().getZookeeperClient();
                    logger.warn("ConnectionState looks something wrong. It will be reset.");
                    zookeeperClient.reset();
                    return;
                } catch (Exception e) {
                    logger.warn("Could not reset connection. cause:{}", e.getMessage(), e);
                }
            }
            final ConnectionState connectionState = connectionManager.getConnectionState();
            throw new ConnectionException("Instance must be connected. connectionState:" + connectionState);
        } else {
            notConnectedStatus.reset();
        }
    }

    private void assertPathHasLength(String path) {
        Assert.hasLength(path, "path must not be empty");
    }

    public boolean isConnected() {
        if (!connectionManager.isConnected() || !stateContext.isStarted()) {
            return false;
        }

        return true;
    }

}

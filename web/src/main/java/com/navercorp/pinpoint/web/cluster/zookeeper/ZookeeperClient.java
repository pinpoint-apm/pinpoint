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

package com.navercorp.pinpoint.web.cluster.zookeeper;


import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConnectionManager;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstatns;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperEventWatcher;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperExceptionResolver;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.ConnectionException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.UnknownException;
import com.navercorp.pinpoint.common.server.util.concurrent.CommonStateContext;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <strong>Conditional thread safe</strong> <br>
 * If multiple threads invokes connect, reconnect, or close concurrently; then it is possible for the object's zookeeper field to be out of sync.
 *
 * @author koo.taejin
 */
public class ZookeeperClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommonStateContext stateContext = new CommonStateContext();

    private final HashedWheelTimer timer;

    private final ZookeeperConnectionManager zookeeperConnectionManager;

    private final long reconnectDelayWhenSessionExpired;

    // hmm this structure should contain all necessary information
    public ZookeeperClient(String hostPort, int sessionTimeout, ZookeeperEventWatcher manager) {
        this(hostPort, sessionTimeout, manager, ZookeeperConstatns.DEFAULT_RECONNECT_DELAY_WHEN_SESSION_EXPIRED);
    }

    public ZookeeperClient(String hostPort, int sessionTimeout, ZookeeperEventWatcher zookeeperEventWatcher, long reconnectDelayWhenSessionExpired) {
        this.zookeeperConnectionManager = new ZookeeperConnectionManager(hostPort, sessionTimeout, zookeeperEventWatcher);

        this.reconnectDelayWhenSessionExpired = reconnectDelayWhenSessionExpired;
        this.timer = TimerFactory.createHashedWheelTimer(this.getClass().getSimpleName(), 100, TimeUnit.MILLISECONDS, 512);
    }

    public void connect() throws IOException {
        if (stateContext.changeStateInitializing()) {
            zookeeperConnectionManager.start();
            stateContext.changeStateStarted();
        } else {
            logger.warn("connect() failed. error : Illegal State. State may be {}.", stateContext.getCurrentState());
        }
    }

    public void reconnectWhenSessionExpired() {
        if (!stateContext.isStarted()) {
            logger.warn("ZookeeperClient.reconnectWhenSessionExpired() failed. Error: Already closed.");
            return;
        }

        boolean hasConnectedZookeeper = zookeeperConnectionManager.reconnectWhenSessionExpired();
        if (!hasConnectedZookeeper) {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    if (timeout.isCancelled()) {
                        return;
                    }

                    reconnectWhenSessionExpired();
                }
            }, reconnectDelayWhenSessionExpired, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * do not create node in path suffix
     *
     * @throws PinpointZookeeperException
     * @throws InterruptedException
     */
    public void createPath(String path) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = zookeeperConnectionManager.getZookeeper();
        int pos = 1;
        do {
            pos = path.indexOf('/', pos + 1);

            if (pos != -1) {
                try {
                    String subPath = path.substring(0, pos);
                    if (zookeeper.exists(subPath, false) != null) {
                        continue;
                    }

                    zookeeper.create(subPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                } catch (KeeperException exception) {
                    if (exception.code() != Code.NODEEXISTS) {
                        ZookeeperExceptionResolver.resolve(exception, true);
                    }
                }
            } else {
                pos = path.length();
            }
        } while (pos < path.length());
    }

    // we need deep node inspection for verification purpose (node content)
    public String createNode(String zNodePath, byte[] data, CreateMode createMode) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = zookeeperConnectionManager.getZookeeper();
        try {
            if (zookeeper.exists(zNodePath, false) != null) {
                return zNodePath;
            }

            String pathName = zookeeper.create(zNodePath, data, Ids.OPEN_ACL_UNSAFE, createMode);
            return pathName;
        } catch (KeeperException exception) {
            if (exception.code() != Code.NODEEXISTS) {
                ZookeeperExceptionResolver.resolve(exception, true);
            }
        }
        return zNodePath;
    }

    public List<String> getChildren(String path, boolean watch) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = zookeeperConnectionManager.getZookeeper();
        try {
            return zookeeper.getChildren(path, watch);
        } catch (KeeperException exception) {
            if (exception.code() != Code.NONODE) {
                ZookeeperExceptionResolver.resolve(exception, true);
            }
        }

        return Collections.emptyList();
    }

    public byte[] getData(String path) throws PinpointZookeeperException, InterruptedException {
        return getData(path, false);
    }

    public byte[] getData(String path, boolean watch) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = zookeeperConnectionManager.getZookeeper();
        try {
            return zookeeper.getData(path, watch, null);
        } catch (KeeperException exception) {
            ZookeeperExceptionResolver.resolve(exception, true);
        }

        throw new UnknownException("UnknownException.");
    }


    public void delete(String path) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = zookeeperConnectionManager.getZookeeper();
        try {
            zookeeper.delete(path, -1);
        } catch (KeeperException exception) {
            if (exception.code() != Code.NONODE) {
                ZookeeperExceptionResolver.resolve(exception, true);
            }
        }
    }

    public boolean exists(String path) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = zookeeperConnectionManager.getZookeeper();
        try {
            Stat stat = zookeeper.exists(path, false);
            if (stat == null) {
                return false;
            }
        } catch (KeeperException exception) {
            if (exception.code() != Code.NODEEXISTS) {
                ZookeeperExceptionResolver.resolve(exception, true);
            }
        }
        return true;
    }

    private void checkState() throws PinpointZookeeperException {
        if (!zookeeperConnectionManager.isConnected() || !stateContext.isStarted()) {
            throw new ConnectionException("Instance must be connected.");
        }
    }

    public void close() {
        if (stateContext.changeStateDestroying()) {
            if (timer != null) {
                timer.stop();
            }

            zookeeperConnectionManager.stop();
            stateContext.changeStateStopped();
        } else {
            logger.warn("close failed. error : Illegal State. State may be {}.", stateContext.getCurrentState());
        }
    }

}

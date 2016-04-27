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

import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.AuthException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.BadOperationException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.ConnectionException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.NoNodeException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.TimeoutException;
import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.UnknownException;
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
 *
 * <strong>Conditional thread safe</strong> <br>
 * If multiple threads invokes connect, reconnect, or close concurrently; then it is possible for the object's zookeeper field to be out of sync.
 *
 * @author koo.taejin
 */
public class DefaultZookeeperClient implements ZookeeperClient {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CommonStateContext stateContext;

    private final HashedWheelTimer timer;
    
    private final String hostPort;
    private final int sessionTimeout;
    private final ZookeeperEventWatcher watcher;
    private final long reconnectDelayWhenSessionExpired;
    
    // Zookeeper clients are thread-safe
    private volatile ZooKeeper zookeeper;
    
    public DefaultZookeeperClient(String hostPort, int sessionTimeout, ZookeeperEventWatcher watcher) {
        this(hostPort, sessionTimeout, watcher, ZookeeperClusterService.DEFAULT_RECONNECT_DELAY_WHEN_SESSION_EXPIRED);
    }

    public DefaultZookeeperClient(String hostPort, int sessionTimeout, ZookeeperEventWatcher watcher, long reconnectDelayWhenSessionExpired) {
        this.hostPort = hostPort;
        this.sessionTimeout = sessionTimeout;
        this.watcher = watcher;
        this.reconnectDelayWhenSessionExpired = reconnectDelayWhenSessionExpired;

        this.timer = TimerFactory.createHashedWheelTimer(this.getClass().getSimpleName(), 100, TimeUnit.MILLISECONDS, 512);
        this.stateContext = new CommonStateContext();
    }

    @Override
    public void connect() throws IOException {
        if (stateContext.changeStateInitializing()) {
            this.zookeeper = new ZooKeeper(hostPort, sessionTimeout, watcher); // server
            stateContext.changeStateStarted();
        } else {
            logger.warn("connect() failed. error : Illegal State. State may be {}.", stateContext.getCurrentState());
        }
    }

    @Override
    public void reconnectWhenSessionExpired() {
        if (!stateContext.isStarted()) {
            logger.warn("ZookeeperClient.reconnectWhenSessionExpired() failed. Error: Already closed.");
            return;
        }
        
        ZooKeeper zookeeper = this.zookeeper;
        if (zookeeper.getState().isConnected()) {
            logger.warn("ZookeeperClient.reconnect() failed. Error: session(0x{}) is connected.", Long.toHexString(zookeeper.getSessionId()));
            return;
        }

        logger.warn("Execute ZookeeperClient.reconnectWhenSessionExpired()(Expired session:0x{}).", Long.toHexString(zookeeper.getSessionId()));
        try {
            zookeeper.close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        ZooKeeper newZookeeper = createNewZookeeper();
        if (!stateContext.isStarted()) {
            logger.warn("ZookeeperClient.reconnectWhenSessionExpired() failed. Error: Already closed.");
            return;
        }

        if (newZookeeper == null) {
            logger.warn("Failed to create new Zookeeper instance. It will be retry  AFTER {}ms.", reconnectDelayWhenSessionExpired);
            
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    if (timeout.isCancelled()) {
                        return;
                    }
                    
                    reconnectWhenSessionExpired();
                }
            }, reconnectDelayWhenSessionExpired, TimeUnit.MILLISECONDS);
        } else {
            this.zookeeper = newZookeeper;
        }
    }
    
    private ZooKeeper createNewZookeeper() {
        try {
            return new ZooKeeper(hostPort, sessionTimeout, watcher);
        } catch (IOException ignore) {
            // ignore
        }
        return null;
    }
    
    /**
     * do not create the final node in the given path.
     *
     * @throws PinpointZookeeperException
     * @throws InterruptedException
     */
    @Override
    public void createPath(String path) throws PinpointZookeeperException, InterruptedException {
        createPath(path, false);
    }

    @Override
    public void createPath(String path, boolean createEndNode) throws PinpointZookeeperException, InterruptedException {
        checkState();

        int pos = 1;
        do {
            pos = path.indexOf('/', pos + 1);

            if (pos == -1) {
                pos = path.length();
            }

            ZooKeeper zookeeper = this.zookeeper;
            try {
                if (pos == path.length()) {
                    if (!createEndNode) {
                        return;
                    }
                }

                String subPath = path.substring(0, pos);
                if (zookeeper.exists(subPath, false) != null) {
                    continue;
                }

                zookeeper.create(subPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } catch (KeeperException exception) {
                if (exception.code() != Code.NODEEXISTS) {
                    handleException(exception);
                }
            }

        } while (pos < path.length());
    }

    @Override
    public String createNode(String zNodePath, byte[] data) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = this.zookeeper;
        try {
            if (zookeeper.exists(zNodePath, false) != null) {
                return zNodePath;
            }

            String pathName = zookeeper.create(zNodePath, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return pathName;
        } catch (KeeperException exception) {
            handleException(exception);
        }
        return zNodePath;
    }

    @Override
    public byte[] getData(String path) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = this.zookeeper;
        try {
            return zookeeper.getData(path, false, null);
        } catch (KeeperException exception) {
            handleException(exception);
        }

        throw new UnknownException("UnknownException.");
    }

    @Override
    public void setData(String path, byte[] data) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = this.zookeeper;
        try {
            if (zookeeper.exists(path, false) == null) {
                return;
            }

            zookeeper.setData(path, data, -1);
        } catch (KeeperException exception) {
            handleException(exception);
        }
    }

    @Override
    public void delete(String path) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = this.zookeeper;
        try {
            zookeeper.delete(path, -1);
        } catch (KeeperException exception) {
            if (exception.code() != Code.NONODE) {
                handleException(exception);
            }
        }
    }

    @Override
    public boolean exists(String path) throws PinpointZookeeperException, InterruptedException {
        checkState();

        ZooKeeper zookeeper = this.zookeeper;
        try {
            Stat stat = zookeeper.exists(path, false);
            if (stat == null) {
                return false;
            }
        } catch (KeeperException exception) {
            if (exception.code() != Code.NODEEXISTS) {
                handleException(exception);
            }
        }
        return true;
    }

    private void checkState() throws PinpointZookeeperException {
        if (!isConnected()) {
            throw new ConnectionException("Instance must be connected.");
        }
    }

    @Override
    public boolean isConnected() {
        if (!watcher.isConnected() || !stateContext.isStarted()) {
            return false;
        }

        return true;
    }

    @Override
    public List<String> getChildrenNode(String path, boolean watch) throws PinpointZookeeperException, InterruptedException {
        checkState();

        try {
            List<String> childNodeList = zookeeper.getChildren(path, watch, null);

            logger.info("ChildNode List = {}", childNodeList);
            return childNodeList;
        } catch (KeeperException exception) {
            if (exception.code() != Code.NONODE) {
                handleException(exception);
            }
        }

        return Collections.emptyList();
    }

    private void handleException(KeeperException keeperException) throws PinpointZookeeperException {
        switch (keeperException.code()) {
            case CONNECTIONLOSS:
            case SESSIONEXPIRED:
                throw new ConnectionException(keeperException.getMessage(), keeperException);
            case AUTHFAILED:
            case INVALIDACL:
            case NOAUTH:
                throw new AuthException(keeperException.getMessage(), keeperException);
            case BADARGUMENTS:
            case BADVERSION:
            case NOCHILDRENFOREPHEMERALS:
            case NOTEMPTY:
            case NODEEXISTS:
                throw new BadOperationException(keeperException.getMessage(), keeperException);
            case NONODE:
                throw new NoNodeException(keeperException.getMessage(), keeperException);
            case OPERATIONTIMEOUT:
                throw new TimeoutException(keeperException.getMessage(), keeperException);
            default:
                throw new UnknownException(keeperException.getMessage(), keeperException);
            }
    }

    @Override
    public void close() {
        if (stateContext.changeStateDestroying()) {
            ZooKeeper zookeeper = this.zookeeper;
            if (timer != null) {
                timer.stop();
            }

            if (zookeeper != null) {
                try {
                    zookeeper.close();
                } catch (InterruptedException ignore) {
                    logger.info("Interrupted zookeeper.close(). Caused:" + ignore.getMessage(), ignore);
                    Thread.currentThread().interrupt();
                }
            }
            stateContext.changeStateStopped();
        }
    }

}

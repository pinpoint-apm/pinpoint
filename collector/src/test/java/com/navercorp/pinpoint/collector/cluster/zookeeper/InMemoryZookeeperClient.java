/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.BadOperationException;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;

import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class InMemoryZookeeperClient implements ZookeeperClient {

    private final AtomicInteger intAdder = new AtomicInteger(0);
    private final boolean throwException;

    private final byte[] EMPTY_BYTE = new byte[]{};
    private final Map<String, byte[]> contents = new HashMap<>();
    private volatile boolean connected = false;

    public InMemoryZookeeperClient() {
        this(false);
    }

    public InMemoryZookeeperClient(boolean throwException) {
        this.throwException = throwException;
    }

    @Override
    public void connect() throws IOException {
        connected = true;
    }

    @Override
    public synchronized void createPath(String value) throws PinpointZookeeperException, InterruptedException {
        ZKPaths.PathAndNode pathAndNode = ZKPaths.getPathAndNode(value);
        contents.put(pathAndNode.getPath(), EMPTY_BYTE);
    }

    @Override
    public synchronized void createNode(CreateNodeMessage createNodeMessage) throws PinpointZookeeperException, InterruptedException {
        byte[] bytes = contents.putIfAbsent(createNodeMessage.getNodePath(), createNodeMessage.getData());
        if (bytes != null) {
            throw new BadOperationException("node already exist");
        }
    }

    @Override
    public synchronized void createOrSetNode(CreateNodeMessage createNodeMessage) throws PinpointZookeeperException, KeeperException, InterruptedException {
        if (intAdder.incrementAndGet() % 2 == 1 && throwException) {
            throw new PinpointZookeeperException("exception");
        }

        contents.put(createNodeMessage.getNodePath(), createNodeMessage.getData());
    }

    @Override
    public synchronized byte[] getData(String path) throws PinpointZookeeperException, InterruptedException {
        byte[] bytes = contents.get(path);
        return bytes;
    }

    @Override
    public byte[] getData(String path, boolean watch) throws PinpointZookeeperException, InterruptedException {
        return contents.get(path);
    }

    @Override
    public synchronized void delete(String path) throws PinpointZookeeperException, InterruptedException {
        contents.remove(path);
    }

    @Override
    public synchronized boolean isConnected() {
        return connected;
    }

    @Override
    public List<String> getChildNodeList(String path, boolean watch) {
        return new ArrayList<>();
    }

    @Override
    public synchronized void close() {
        connected = false;
    }

}

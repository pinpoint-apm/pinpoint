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

import com.navercorp.pinpoint.collector.cluster.zookeeper.exception.PinpointZookeeperException;

import java.io.IOException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public interface ZookeeperClient {

    void connect() throws IOException;

    void reconnectWhenSessionExpired();

    void createPath(String path) throws PinpointZookeeperException, InterruptedException;

    void createPath(String path, boolean createEndNode) throws PinpointZookeeperException, InterruptedException;

    String createNode(String zNodePath, byte[] data) throws PinpointZookeeperException, InterruptedException;

    byte[] getData(String path) throws PinpointZookeeperException, InterruptedException;

    void setData(String path, byte[] data) throws PinpointZookeeperException, InterruptedException;

    void delete(String path) throws PinpointZookeeperException, InterruptedException;

    boolean exists(String path) throws PinpointZookeeperException, InterruptedException;

    boolean isConnected();

    List<String> getChildrenNode(String path, boolean watch) throws PinpointZookeeperException, InterruptedException;

    void close();

 }

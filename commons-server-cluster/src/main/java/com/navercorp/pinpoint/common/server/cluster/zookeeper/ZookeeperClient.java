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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.exception.PinpointZookeeperException;

import java.io.Closeable;
import java.util.List;

/**
 * @author Taejin Koo
 */
public interface ZookeeperClient extends Closeable {

    void connect() throws PinpointZookeeperException;

    void createPath(String path) throws PinpointZookeeperException;
    void createNode(CreateNodeMessage createNodeMessage) throws PinpointZookeeperException;
    void createOrSetNode(CreateNodeMessage createNodeMessage) throws PinpointZookeeperException;

    byte[] getData(String path) throws PinpointZookeeperException;
    byte[] getData(String path, boolean watch) throws PinpointZookeeperException;
    List<String> getChildNodeList(String path, boolean watch) throws PinpointZookeeperException;

    void delete(String path) throws PinpointZookeeperException;

    boolean isConnected();

    void close();

}

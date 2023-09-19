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

package com.navercorp.pinpoint.flink.cluster.zookeeper;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ZookeeperClusterDataManagerHelper {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ZookeeperClient client;

    public ZookeeperClusterDataManagerHelper(ZookeeperClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    public boolean pushZNode(CreateNodeMessage createNodeMessage) {
        Objects.requireNonNull(createNodeMessage, "createNodeMessage");

        try {
            String nodePath = createNodeMessage.getNodePath();
            client.createPath(nodePath);
            client.createOrSetNode(createNodeMessage);
            logger.info("Register Zookeeper node UniqPath = {}.", nodePath);
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
    }

}

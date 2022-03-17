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

import com.navercorp.pinpoint.common.server.cluster.zookeeper.CreateNodeMessage;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClient;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import com.navercorp.pinpoint.web.cluster.ClusterId;
import org.apache.curator.utils.ZKPaths;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    Map<ClusterId, byte[]> syncPullCollectorCluster(String parentPath) {
        try {
            List<String> collectorList = client.getChildNodeList(parentPath, true);
            if (CollectionUtils.isEmpty(collectorList)) {
                return Collections.emptyMap();
            }

            Map<ClusterId, byte[]> map = new HashMap<>();

            for (String collectorId : collectorList) {
                String fullPath = ZKPaths.makePath(parentPath, collectorId);

                byte[] data = client.getData(fullPath, true);
                map.put(ClusterId.newClusterId(parentPath, collectorId), data);
            }

            return map;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return Collections.emptyMap();
    }


    public boolean pushZnode(CreateNodeMessage createNodeMessage) {
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

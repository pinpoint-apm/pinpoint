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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.MapUtils;

import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ZookeeperClusterDataManagerHelper {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ZookeeperClusterDataManagerHelper() {
    }

    Map<String, byte[]> getCollectorData(ZookeeperClient client, String parentPath) {
        try {
            List<String> collectorList = client.getChildNodeList(parentPath, true);
            if (CollectionUtils.isEmpty(collectorList)) {
                return Collections.emptyMap();
            }

            Map<String, byte[]> map = new HashMap<>();

            for (String collector : collectorList) {
                String fullPath = ZKPaths.makePath(parentPath, collector);

                byte[] data = client.getData(fullPath, true);
                map.put(fullPath, data);
            }

            return map;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return Collections.emptyMap();
    }

    String extractCollectorClusterId(String path, String collectorClusterPath) {
        int index = path.indexOf(collectorClusterPath);

        int startPosition = index + collectorClusterPath.length() + 1;

        if (path.length() > startPosition) {
            String id = path.substring(startPosition);
            return id;
        }

        return null;
    }

    public boolean pushZnode(ZookeeperClient client, CreateNodeMessage createNodeMessage) {
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

    Map<String, byte[]> syncPullCollectorCluster(ZookeeperClient client, String path) {
        Map<String, byte[]> map = getCollectorData(client, path);
        if (MapUtils.isEmpty(map)) {
            return Collections.emptyMap();
        }

        Map<String, byte[]> result = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            String key = entry.getKey();
            byte[] value = entry.getValue();

            String id = extractCollectorClusterId(key, path);
            if (id == null) {
                logger.error("Illegal Collector Path({}) found.", key);
                continue;
            }
            result.put(id, value);
        }

        return result;
    }

}

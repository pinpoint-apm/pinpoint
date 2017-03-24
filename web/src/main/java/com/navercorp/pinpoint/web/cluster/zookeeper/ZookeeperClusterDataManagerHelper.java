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

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ZookeeperClusterDataManagerHelper {

    private static final String PATH_SEPARATOR = "/";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ZookeeperClusterDataManagerHelper() {
    }

    Map<String, byte[]> getCollectorData(ZookeeperClient client, String path) {
        try {
            List<String> collectorList = client.getChildren(path, true);
            if (collectorList == Collections.EMPTY_LIST) {
                return Collections.emptyMap();
            }

            Map<String, byte[]> map = new HashMap<>();

            for (String collector : collectorList) {
                String node = bindingPathAndZNode(path, collector);

                byte[] data = client.getData(node, true);
                map.put(node, data);
            }

            return map;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return Collections.emptyMap();
    }

    public String bindingPathAndZNode(String path, String zNodeName) {
        StringBuilder fullPath = new StringBuilder();

        fullPath.append(path);
        if (!path.endsWith(PATH_SEPARATOR)) {
            fullPath.append(PATH_SEPARATOR);
        }
        fullPath.append(zNodeName);

        return fullPath.toString();
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

    public boolean pushZnode(ZookeeperClient client, PushZnodeJob job) {
        if (job == null) {
            return false;
        }
        
        String zNodePath = job.getZNodePath();
        byte[] contents = job.getContents();

        try {
            if (!client.exists(zNodePath)) {
                client.createPath(zNodePath);
            }

            // ip:port zNode naming scheme
            String nodeName = client.createNode(zNodePath, contents, CreateMode.EPHEMERAL);
            logger.info("Register Zookeeper node UniqPath = {}.", zNodePath);
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return false;
    }

    Map<String, byte[]> syncPullCollectorCluster(ZookeeperClient client, String path) {
        Map<String, byte[]> map = getCollectorData(client, path);
        if (map == Collections.EMPTY_MAP) {
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

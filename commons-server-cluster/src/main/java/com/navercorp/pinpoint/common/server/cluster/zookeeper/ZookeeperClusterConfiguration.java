/*
 * Copyright 2021 NAVER Corp.
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

import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.common.PathUtils;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ZookeeperClusterConfiguration {

    private final boolean enable;

    private final String address;
    private final int sessionTimeout;

    private final String webZNodePath;
    private final String collectorZNodePath;
    private final String flinkZNodePath;

    private ZookeeperClusterConfiguration(Builder builder) {
        this.enable = builder.enable;
        this.address = builder.address;
        this.sessionTimeout = builder.sessionTimeout;

        String zNodeRootPath = builder.zNodeRoot;
        this.webZNodePath = ZKPaths.makePath(zNodeRootPath, builder.webLeafPath);
        this.collectorZNodePath = ZKPaths.makePath(zNodeRootPath, builder.collectorLeafPath);
        this.flinkZNodePath = ZKPaths.makePath(zNodeRootPath, builder.flinkLeafPath);
    }

    public boolean isEnable() {
        return enable;
    }

    public String getAddress() {
        return address;
    }

    public String getWebZNodePath() {
        return webZNodePath;
    }

    public String getCollectorZNodePath() {
        return collectorZNodePath;
    }

    public String getFlinkZNodePath() {
        return flinkZNodePath;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private boolean enable = false;
        private String address = "localhost";
        private String zNodeRoot = ZookeeperConstants.DEFAULT_CLUSTER_ZNODE_ROOT_PATH;
        private String webLeafPath = ZookeeperConstants.WEB_LEAF_PATH;
        private String collectorLeafPath = ZookeeperConstants.COLLECTOR_LEAF_PATH;
        private String flinkLeafPath = ZookeeperConstants.FLINK_LEAF_PATH;

        private int sessionTimeout = 3000;

        public Builder() {
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getZnodeRoot() {
            return zNodeRoot;
        }

        public void setZnodeRoot(String zNodeRoot) {
            this.zNodeRoot = zNodeRoot;
        }

        public String getWebLeafPath() {
            return webLeafPath;
        }

        public void setWebLeafPath(String webLeafPath) {
            this.webLeafPath = webLeafPath;
        }

        public String getCollectorLeafPath() {
            return collectorLeafPath;
        }

        public void setCollectorLeafPath(String collectorLeafPath) {
            this.collectorLeafPath = collectorLeafPath;
        }

        public String getFlinkLeafPath() {
            return flinkLeafPath;
        }

        public void setFlinkLeafPath(String flinkLeafPath) {
            this.flinkLeafPath = flinkLeafPath;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public ZookeeperClusterConfiguration build() {
            Objects.requireNonNull(address);
            PathUtils.validatePath(zNodeRoot);
            Assert.isTrue(sessionTimeout > 0, "sessionTimeout must be greater than 0");

            return new ZookeeperClusterConfiguration(this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ZookeeperClusterConfiguration{");
        sb.append("enable=").append(enable);
        sb.append(", address='").append(address).append('\'');
        sb.append(", sessionTimeout=").append(sessionTimeout);
        sb.append(", webZNodePath='").append(webZNodePath).append('\'');
        sb.append(", collectorZNodePath='").append(collectorZNodePath).append('\'');
        sb.append(", flinkZNodePath='").append(flinkZNodePath).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

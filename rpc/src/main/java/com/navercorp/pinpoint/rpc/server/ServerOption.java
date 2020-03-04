/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;

/**
 * @author Taejin Koo
 */
public class ServerOption {

    private static final long DEFAULT_REQUEST_TIMEOUT_MILLIS = 3 * 1000;
    private static final long DEFAULT_SERVER_CLOSE_WAIT_TIMEOUT_MILLIS = 3 * 1000;
    private static final long DEFAULT_HEALTH_CHECK_INTERVAL_TIME_MILLIS = 5 * 60 * 1000;
    private static final long DEFAULT_HEALTH_CHECK_PACKET_WAIT_TIME_MILLIS = 30 * 60 * 1000;
    private static final ClusterOption DEFAULT_CLUSTER_OPTION = ClusterOption.DISABLE_CLUSTER_OPTION;

    private static final ServerOption DEFAULT_INSTANCE = new ServerOption.Builder().build();

    private final long requestTimeoutMillis;
    private final long serverCloseWaitTimeoutMillis;
    private final long healthCheckIntervalTimeMillis;
    private final long healthCheckPacketWaitTimeMillis;
    private final ClusterOption clusterOption;

    public static ServerOption getDefaultServerOption() {
        return DEFAULT_INSTANCE;
    }

    private ServerOption(long requestTimeoutMillis, long serverCloseWaitTimeoutMillis, long healthCheckIntervalTimeMillis, long healthCheckPacketWaitTimeMillis, ClusterOption clusterOption) {
        this.requestTimeoutMillis = requestTimeoutMillis;
        this.serverCloseWaitTimeoutMillis = serverCloseWaitTimeoutMillis;
        this.healthCheckIntervalTimeMillis = healthCheckIntervalTimeMillis;
        this.healthCheckPacketWaitTimeMillis = healthCheckPacketWaitTimeMillis;
        this.clusterOption = clusterOption;
    }

    public long getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }

    public long getServerCloseWaitTimeoutMillis() {
        return serverCloseWaitTimeoutMillis;
    }

    public long getHealthCheckIntervalTimeMillis() {
        return healthCheckIntervalTimeMillis;
    }

    public long getHealthCheckPacketWaitTimeMillis() {
        return healthCheckPacketWaitTimeMillis;
    }

    public ClusterOption getClusterOption() {
        return clusterOption;
    }

    @Override
    public String toString() {
        return "ServerOption{" +
                "requestTimeoutMillis=" + requestTimeoutMillis +
                ", serverCloseWaitTimeoutMillis=" + serverCloseWaitTimeoutMillis +
                ", healthCheckIntervalTimeMillis=" + healthCheckIntervalTimeMillis +
                ", healthCheckPacketWaitTimeMillis=" + healthCheckPacketWaitTimeMillis +
                ", clusterOption=" + clusterOption +
                '}';
    }

    public static class Builder {

        private long requestTimeoutMillis = DEFAULT_REQUEST_TIMEOUT_MILLIS;
        private long serverCloseWaitTimeoutMillis = DEFAULT_SERVER_CLOSE_WAIT_TIMEOUT_MILLIS;
        private long healthCheckIntervalTimeMillis = DEFAULT_HEALTH_CHECK_INTERVAL_TIME_MILLIS;
        private long healthCheckPacketWaitTimeMillis = DEFAULT_HEALTH_CHECK_PACKET_WAIT_TIME_MILLIS;
        private ClusterOption clusterOption = DEFAULT_CLUSTER_OPTION;

        public long getRequestTimeoutMillis() {
            return requestTimeoutMillis;
        }

        public void setRequestTimeoutMillis(long requestTimeoutMillis) {
            Assert.isTrue(requestTimeoutMillis > 0, "requestTimeoutMillis cannot be a negative number");
            this.requestTimeoutMillis = requestTimeoutMillis;
        }

        public long getServerCloseWaitTimeoutMillis() {
            return serverCloseWaitTimeoutMillis;
        }

        public void setServerCloseWaitTimeoutMillis(long serverCloseWaitTimeoutMillis) {
            Assert.isTrue(serverCloseWaitTimeoutMillis > 0, "serverCloseWaitTimeoutMillis cannot be a negative number");
            this.serverCloseWaitTimeoutMillis = serverCloseWaitTimeoutMillis;
        }

        public long getHealthCheckIntervalTimeMillis() {
            return healthCheckIntervalTimeMillis;
        }

        public void setHealthCheckIntervalTimeMillis(long healthCheckIntervalTimeMillis) {
            Assert.isTrue(healthCheckIntervalTimeMillis > 0, "healthCheckIntervalTimeMillis cannot be a negative number");
            this.healthCheckIntervalTimeMillis = healthCheckIntervalTimeMillis;
        }

        public long getHealthCheckPacketWaitTimeMillis() {
            return healthCheckPacketWaitTimeMillis;
        }

        public void setHealthCheckPacketWaitTimeMillis(long healthCheckPacketWaitTimeMillis) {
            Assert.isTrue(healthCheckPacketWaitTimeMillis > 0, "healthCheckPacketWaitTimeMillis cannot be a negative number");
            this.healthCheckPacketWaitTimeMillis = healthCheckPacketWaitTimeMillis;
        }

        public ClusterOption getClusterOption() {
            return clusterOption;
        }

        public void setClusterOption(ClusterOption clusterOption) {
            this.clusterOption = Assert.requireNonNull(clusterOption, "clusterOption");
        }

        public ServerOption build() {
            return new ServerOption(requestTimeoutMillis, serverCloseWaitTimeoutMillis, healthCheckIntervalTimeMillis, healthCheckPacketWaitTimeMillis, clusterOption);
        }

    }

}

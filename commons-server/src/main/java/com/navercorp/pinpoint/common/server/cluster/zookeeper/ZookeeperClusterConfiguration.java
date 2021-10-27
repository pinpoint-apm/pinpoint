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

import com.navercorp.pinpoint.common.util.Assert;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ZookeeperClusterConfiguration {

    private final boolean enable;

    private final String address;
    private final int sessionTimeout;

    private ZookeeperClusterConfiguration(Builder builder) {
        this.enable = builder.enable;
        this.address = builder.address;
        this.sessionTimeout = builder.sessionTimeout;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getAddress() {
        return address;
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

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        public ZookeeperClusterConfiguration build() {
            Objects.requireNonNull(address);
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
        sb.append('}');
        return sb.toString();
    }
}

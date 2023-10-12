/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.server.hbase.config;

import org.apache.hadoop.hbase.HConstants;

import java.util.Properties;

public class HBaseClientProperties {


    private String host;
    private int port = HConstants.DEFAULT_ZOOKEEPER_CLIENT_PORT;

    private String znode = HConstants.DEFAULT_ZOOKEEPER_ZNODE_PARENT;


    private Properties properties;

    public HBaseClientProperties() {
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getZnode() {
        return znode;
    }

    public void setZnode(String znode) {
        this.znode = znode;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "HBaseClientProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", znode='" + znode + '\'' +
                ", properties=" + properties +
                '}';
    }
}

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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
@Configuration
public class CollectorConfiguration {

    private final Logger logger = LoggerFactory.getLogger(CollectorConfiguration.class);

    @Value("${collector.agentEventWorker.threadSize:32}")
    private int agentEventWorkerThreadSize;

    @Value("${collector.agentEventWorker.queueSize:5120}")
    private int agentEventWorkerQueueSize;

//    @Value("#{'${collector.l4.ip:}'.split(',')}")
//    private List<String> l4IpList = Collections.emptyList();
    @Value("${collector.l4.ip:}")
    private String[] l4IpList = new String[0];

    @Value("${cluster.enable}")
    private boolean clusterEnable;

    @Value("${cluster.zookeeper.address:}")
    private String clusterAddress;

    @Value("${cluster.zookeeper.sessiontimeout:-1}")
    private int clusterSessionTimeout;


    @Value("${cluster.listen.ip:}")
    private String clusterListenIp;


    @Value("${cluster.listen.port:-1}")
    private int clusterListenPort;

    public int getAgentEventWorkerThreadSize() {
        return this.agentEventWorkerThreadSize;
    }

    public void setAgentEventWorkerThreadSize(int agentEventWorkerThreadSize) {
        this.agentEventWorkerThreadSize = agentEventWorkerThreadSize;
    }

    public int getAgentEventWorkerQueueSize() {
        return agentEventWorkerQueueSize;
    }

    public void setAgentEventWorkerQueueSize(int agentEventWorkerQueueSize) {
        this.agentEventWorkerQueueSize = agentEventWorkerQueueSize;
    }

    public List<String> getL4IpList() {
        return Arrays.asList(l4IpList);
    }

    public void setL4IpList(List<String> l4IpList) {
        Objects.requireNonNull(l4IpList, "l4IpList");
        this.l4IpList = l4IpList.toArray(new String[0]);
    }

    public boolean isClusterEnable() {
        return clusterEnable;
    }

    public void setClusterEnable(boolean clusterEnable) {
        this.clusterEnable = clusterEnable;
    }

    public String getClusterAddress() {
        return clusterAddress;
    }

    public void setClusterAddress(String clusterAddress) {
        this.clusterAddress = clusterAddress;
    }

    public int getClusterSessionTimeout() {
        return clusterSessionTimeout;
    }

    public void setClusterSessionTimeout(int clusterSessionTimeout) {
        this.clusterSessionTimeout = clusterSessionTimeout;
    }

    public String getClusterListenIp() {
        return clusterListenIp;
    }

    public void setClusterListenIp(String clusterListenIp) {
        this.clusterListenIp = clusterListenIp;
    }

    public int getClusterListenPort() {
        return clusterListenPort;
    }

    public void setClusterListenPort(int clusterListenPort) {
        this.clusterListenPort = clusterListenPort;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor visitor = new AnnotationVisitor(Value.class);
        visitor.visit(this, new LoggingEvent(logger));
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CollectorConfiguration{");
        sb.append("agentEventWorkerThreadSize=").append(agentEventWorkerThreadSize);
        sb.append(", agentEventWorkerQueueSize=").append(agentEventWorkerQueueSize);
        sb.append(", l4IpList=").append(Arrays.toString(l4IpList));
        sb.append(", clusterEnable=").append(clusterEnable);
        sb.append(", clusterAddress='").append(clusterAddress).append('\'');
        sb.append(", clusterSessionTimeout=").append(clusterSessionTimeout);
        sb.append(", clusterListenIp='").append(clusterListenIp).append('\'');
        sb.append(", clusterListenPort=").append(clusterListenPort);
        sb.append('}');
        return sb.toString();
    }

}

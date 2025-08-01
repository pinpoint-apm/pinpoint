/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.agentstatistics.collector.entity;

import java.util.List;

/**
 * @author intr3p1d
 */
public class AgentInfoEntity {

    // AgentInfoBo
    private String hostName;
    private String ip;
    private String agentId;
    private String agentName;
    private String applicationName;
    private int serviceTypeCode;
    private int pid;
    private String vmVersion;
    private String agentVersion;

    private long startTime;

    private long endTimeStamp;
    private int endStatus;

    private boolean container;

    // ServerMetaDataBo
    private String serverInfo;
    private List<String> vmArgs;

    // ServiceInfoBo
    private List<String> serviceInfos;
    private String serviceInfosString;

    // JvmInfoBo
    private int version;
    private String jvmVersion;
    private String gcTypeName;

    public AgentInfoEntity() {
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getServiceTypeCode() {
        return serviceTypeCode;
    }

    public void setServiceTypeCode(int serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getVmVersion() {
        return vmVersion;
    }

    public void setVmVersion(String vmVersion) {
        this.vmVersion = vmVersion;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public int getEndStatus() {
        return endStatus;
    }

    public void setEndStatus(int endStatus) {
        this.endStatus = endStatus;
    }

    public boolean isContainer() {
        return container;
    }

    public void setContainer(boolean container) {
        this.container = container;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }

    public List<String> getVmArgs() {
        return vmArgs;
    }

    public void setVmArgs(List<String> vmArgs) {
        this.vmArgs = vmArgs;
    }

    public List<String> getServiceInfos() {
        return serviceInfos;
    }

    public void setServiceInfos(List<String> serviceInfos) {
        this.serviceInfos = serviceInfos;
    }

    public String getServiceInfosString() {
        return serviceInfosString;
    }

    public void setServiceInfosString(String serviceInfosString) {
        this.serviceInfosString = serviceInfosString;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public String getGcTypeName() {
        return gcTypeName;
    }

    public void setGcTypeName(String gcTypeName) {
        this.gcTypeName = gcTypeName;
    }
}

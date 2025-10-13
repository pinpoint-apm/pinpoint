/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class AgentUriStatBo {
    private final byte bucketVersion;
    @NonNull
    private final String serviceName;
    @NonNull
    private final String applicationName;
    @NonNull
    private final String agentId;

    private final List<EachUriStatBo> eachUriStatBoList;

    public AgentUriStatBo(byte bucketVersion,
                          String serviceName,
                          String applicationName,
                          String agentId,
                          List<EachUriStatBo> eachUriStatBoList) {
        this.bucketVersion = bucketVersion;
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.eachUriStatBoList = Objects.requireNonNull(eachUriStatBoList, "eachUriStatBoList");
    }

    public byte getBucketVersion() {
        return bucketVersion;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public List<EachUriStatBo> getEachUriStatBoList() {
        return eachUriStatBoList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        AgentUriStatBo that = (AgentUriStatBo) o;
        return bucketVersion == that.bucketVersion && serviceName.equals(that.serviceName) && applicationName.equals(that.applicationName) && agentId.equals(that.agentId) && eachUriStatBoList.equals(that.eachUriStatBoList);
    }

    @Override
    public int hashCode() {
        int result = bucketVersion;
        result = 31 * result + serviceName.hashCode();
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + agentId.hashCode();
        result = 31 * result + eachUriStatBoList.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AgentUriStatBo{" +
                "serviceName='" + serviceName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", bucketVersion=" + bucketVersion +
                ", eachUriStatBoList=" + eachUriStatBoList +
                '}';
    }
}

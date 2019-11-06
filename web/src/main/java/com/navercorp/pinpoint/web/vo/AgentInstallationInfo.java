/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.AgentInstallationInfoSerializer;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = AgentInstallationInfoSerializer.class)
public class AgentInstallationInfo {

    private static final String PINPOINT_JAVA_AGENT_ARGUMENT = "-javaagent:${pinpointPath}/pinpoint-bootstrap-%s.jar";

    private final AgentDownloadInfo agentDownloadInfo;
    private final String javaInstallationInfo;

    public AgentInstallationInfo(AgentDownloadInfo agentDownloadInfo) {
        this.agentDownloadInfo = Objects.requireNonNull(agentDownloadInfo, "agentDownloadInfo");
        this.javaInstallationInfo = String.format(PINPOINT_JAVA_AGENT_ARGUMENT, agentDownloadInfo.getVersion());
    }

    public String getVersion() {
        return agentDownloadInfo.getVersion();
    }

    public String getDownloadUrl() {
        return agentDownloadInfo.getDownloadUrl();
    }

    public String getJavaInstallationInfo() {
        return javaInstallationInfo;
    }

}

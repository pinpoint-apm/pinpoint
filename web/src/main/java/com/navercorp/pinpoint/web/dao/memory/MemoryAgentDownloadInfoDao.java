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

package com.navercorp.pinpoint.web.dao.memory;

import com.navercorp.pinpoint.web.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class MemoryAgentDownloadInfoDao implements AgentDownloadInfoDao {

    private final List<AgentDownloadInfo> agentDownloadInfoList;

    public MemoryAgentDownloadInfoDao(String version, String downloadUrl) {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(downloadUrl, "downloadUrl");

        AgentDownloadInfo agentDownloadInfo = new AgentDownloadInfo(version, downloadUrl);

        this.agentDownloadInfoList = Collections.singletonList(agentDownloadInfo);
    }

    @Override
    public List<AgentDownloadInfo> getDownloadInfoList() {
        return agentDownloadInfoList;
    }

}

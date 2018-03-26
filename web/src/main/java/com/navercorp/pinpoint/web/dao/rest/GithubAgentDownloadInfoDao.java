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

package com.navercorp.pinpoint.web.dao.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;
import com.navercorp.pinpoint.web.vo.GithubAgentDownloadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Taejin Koo
 */
public class GithubAgentDownloadInfoDao implements AgentDownloadInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static String GITHUB_API_URL = "https://api.github.com/repos/naver/pinpoint/releases";

    private static final Pattern STABLE_VERSION_PATTERN = Pattern.compile(IdValidateUtils.STABLE_VERSION_PATTERN_VALUE);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<AgentDownloadInfo> getDownloadInfoList() {
        List<AgentDownloadInfo> result = new ArrayList<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            String responseBody = restTemplate.getForObject(GITHUB_API_URL, String.class);

            TypeReference<List<GithubAgentDownloadInfo>> typeReference = new TypeReference<List<GithubAgentDownloadInfo>>() {};
            List<GithubAgentDownloadInfo> agentDownloadInfoList = objectMapper.readValue(responseBody, typeReference);

            if (CollectionUtils.isEmpty(agentDownloadInfoList)) {
                return result;
            }

            for (GithubAgentDownloadInfo agentDownloadInfo : agentDownloadInfoList) {
                if (STABLE_VERSION_PATTERN.matcher(agentDownloadInfo.getVersion()).matches()) {
                    result.add(agentDownloadInfo);
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return result;
    }

}

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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;
import com.navercorp.pinpoint.web.vo.GithubAgentDownloadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Taejin Koo
 */
@Repository
public class GithubAgentDownloadInfoDao implements AgentDownloadInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static String GITHUB_API_URL = "https://api.github.com/repos/naver/pinpoint/releases";

    private static final Pattern STABLE_VERSION_PATTERN = Pattern.compile(IdValidateUtils.STABLE_VERSION_PATTERN_VALUE);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public List<AgentDownloadInfo> getDownloadInfoList() {
        RestTemplate restTemplate = new RestTemplate();

        String responseBody = restTemplate.getForObject(GITHUB_API_URL, String.class);
        JavaType agentDownloadInfoListType = objectMapper.getTypeFactory().constructCollectionType(List.class, GithubAgentDownloadInfo.class);

        List<AgentDownloadInfo> result = new ArrayList<>();
        try {
            List<GithubAgentDownloadInfo> agentDownloadInfoList = objectMapper.readValue(responseBody, agentDownloadInfoListType);

            for (GithubAgentDownloadInfo agentDownloadInfo : agentDownloadInfoList) {
                if (STABLE_VERSION_PATTERN.matcher(agentDownloadInfo.getVersion()).matches()) {
                    result.add(agentDownloadInfo);
                }
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }

        return result;
    }

}

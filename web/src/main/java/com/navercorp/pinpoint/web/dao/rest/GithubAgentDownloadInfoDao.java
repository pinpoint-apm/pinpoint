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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;
import com.navercorp.pinpoint.web.vo.GithubAgentDownloadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Taejin Koo
 */
public class GithubAgentDownloadInfoDao implements AgentDownloadInfoDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String GITHUB_API_URL = "https://api.github.com/repos/naver/pinpoint/releases";

    private static final Pattern STABLE_VERSION_PATTERN = Pattern.compile(IdValidateUtils.STABLE_VERSION_PATTERN_VALUE);
    private static final ParameterizedTypeReference<List<GithubAgentDownloadInfo>> responseType
            = new ParameterizedTypeReference<List<GithubAgentDownloadInfo>>() {};

    private final RestTemplate restTemplate;

    public GithubAgentDownloadInfoDao(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<AgentDownloadInfo> getDownloadInfoList() {
        List<AgentDownloadInfo> result = new ArrayList<>();
        try {
            ResponseEntity<List<GithubAgentDownloadInfo>> responseEntity = restTemplate.exchange(GITHUB_API_URL, HttpMethod.GET, null, responseType);
            List<GithubAgentDownloadInfo> agentDownloadInfoList = responseEntity.getBody();

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

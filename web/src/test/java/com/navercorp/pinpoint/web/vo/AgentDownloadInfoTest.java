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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.web.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.dao.AgentDownloadInfoDaoFactory;
import com.navercorp.pinpoint.web.dao.memory.MemoryAgentDownloadInfoDao;
import com.navercorp.pinpoint.web.dao.rest.GithubAgentDownloadInfoDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentDownloadInfoTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void factoryTest() {
        String version = "1.6.0";
        String downloadUrl = "http://localhost:8080/pinpoint-agent-1.6.0.tar.gz";

        AgentDownloadInfoDao agentDownloadInfoDao = AgentDownloadInfoDaoFactory.create(version, downloadUrl, restTemplate);
        Assert.assertTrue(agentDownloadInfoDao instanceof MemoryAgentDownloadInfoDao);
        Assert.assertEquals(version, agentDownloadInfoDao.getDownloadInfoList().get(0).getVersion());
        Assert.assertEquals(downloadUrl, agentDownloadInfoDao.getDownloadInfoList().get(0).getDownloadUrl());

        agentDownloadInfoDao = AgentDownloadInfoDaoFactory.create(version, "", restTemplate);
        Assert.assertTrue(agentDownloadInfoDao instanceof GithubAgentDownloadInfoDao);

        agentDownloadInfoDao = AgentDownloadInfoDaoFactory.create("   ", downloadUrl, restTemplate);
        Assert.assertTrue(agentDownloadInfoDao instanceof GithubAgentDownloadInfoDao);
    }

    @Test
    public void defaultTest() throws Exception {
        String mockResponseString = getMockJsonString();

        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<GithubAgentDownloadInfo>> typeReference = new TypeReference<List<GithubAgentDownloadInfo>>() {};
        List<GithubAgentDownloadInfo> agentDownloadInfoList = objectMapper.readValue(mockResponseString, typeReference);

        Assert.assertEquals(15, agentDownloadInfoList.size());
    }

    private String getMockJsonString() throws IOException {
        try (InputStream resourceAsStream = getResourceAsStream("mock/github_pinpoint_release_response.json"))  {
            byte[] bytes = IOUtils.toByteArray(resourceAsStream);
            return BytesUtils.toString(bytes);
        }
    }

    private InputStream getResourceAsStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

}

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

package com.navercorp.pinpoint.web.install;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.web.dao.memory.MemoryAgentDownloadInfoDao;
import com.navercorp.pinpoint.web.dao.rest.GithubAgentDownloadInfoDao;
import com.navercorp.pinpoint.web.install.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.install.dao.AgentDownloadInfoDaoFactoryBean;
import com.navercorp.pinpoint.web.install.model.GithubAgentDownloadInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentDownloadInfoTest {

    String version = "1.6.0";
    String downloadUrl = "http://localhost:8080/pinpoint-agent-1.6.0.tar.gz";
    RestTemplate restTemplate = new RestTemplate();

    @Test
    void factoryTest1() throws Exception {
        AgentDownloadInfoDaoFactoryBean factoryBean = new AgentDownloadInfoDaoFactoryBean();
        factoryBean.setVersion(version);
        factoryBean.setDownloadUrl(downloadUrl);
        factoryBean.setRestTemplate(restTemplate);

        AgentDownloadInfoDao dao = factoryBean.getObject();
        Assertions.assertTrue(dao instanceof MemoryAgentDownloadInfoDao);
        Assertions.assertEquals(version, dao.getDownloadInfoList().get(0).getVersion());
        Assertions.assertEquals(downloadUrl, dao.getDownloadInfoList().get(0).getDownloadUrl());
    }

    @Test
    void factoryTest2() throws Exception {
        AgentDownloadInfoDaoFactoryBean factoryBean = new AgentDownloadInfoDaoFactoryBean();
        factoryBean.setVersion(version);
        factoryBean.setDownloadUrl("");
        factoryBean.setRestTemplate(restTemplate);

        AgentDownloadInfoDao dao = factoryBean.getObject();
        Assertions.assertTrue(dao instanceof GithubAgentDownloadInfoDao);
    }

    @Test
    void factoryTest3() throws Exception {
        AgentDownloadInfoDaoFactoryBean factoryBean = new AgentDownloadInfoDaoFactoryBean();
        factoryBean.setVersion("    ");
        factoryBean.setDownloadUrl(downloadUrl);
        factoryBean.setRestTemplate(restTemplate);

        AgentDownloadInfoDao dao = factoryBean.getObject();
        Assertions.assertTrue(dao instanceof GithubAgentDownloadInfoDao);
    }


    @Test
    void defaultTest() throws Exception {
        String mockResponseString = getMockJsonString();

        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<GithubAgentDownloadInfo>> typeReference = new TypeReference<>() {
        };
        List<GithubAgentDownloadInfo> agentDownloadInfoList = objectMapper.readValue(mockResponseString, typeReference);

        Assertions.assertEquals(15, agentDownloadInfoList.size());
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

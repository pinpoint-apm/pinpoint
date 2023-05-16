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
import com.navercorp.pinpoint.web.install.dao.AgentDownloadInfoDao;
import com.navercorp.pinpoint.web.install.dao.GithubAgentDownloadInfoDao;
import com.navercorp.pinpoint.web.install.dao.MemoryAgentDownloadInfoDao;
import com.navercorp.pinpoint.web.install.model.GithubAgentDownloadInfo;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Taejin Koo
 */
public class AgentDownloadInfoTest {

    String version = "1.6.0";
    String downloadUrl = "http://localhost:8080/pinpoint-agent-1.6.0.tar.gz";
    RestTemplate restTemplate = new RestTemplate();

    @Test
    void factoryTest1() {
        InstallModule module = new InstallModule();

        AgentDownloadInfoDao dao = module.urlAgentDownloadInfoDao(version, downloadUrl);
        assertThat(dao).isInstanceOf(MemoryAgentDownloadInfoDao.class);
        assertEquals(version, dao.getDownloadInfoList().get(0).getVersion());
        assertEquals(downloadUrl, dao.getDownloadInfoList().get(0).getDownloadUrl());
    }

    @Test
    void factoryTest2() {
        InstallModule module = new InstallModule();

        assertThrows(IllegalArgumentException.class,
                () -> module.urlAgentDownloadInfoDao(version, ""));
    }

    @Test
    void factoryTest3() {
        InstallModule module = new InstallModule();

        AgentDownloadInfoDao dao = module.githubAgentDownloadInfoDao(restTemplate);
        assertThat(dao).isInstanceOf(GithubAgentDownloadInfoDao.class);
    }

    @Test
    void defaultTest() throws Exception {
        String mockResponseString = getMockJsonString();

        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<GithubAgentDownloadInfo>> typeReference = new TypeReference<>() {
        };
        List<GithubAgentDownloadInfo> agentDownloadInfoList = objectMapper.readValue(mockResponseString, typeReference);
        assertThat(agentDownloadInfoList).hasSize(15);
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

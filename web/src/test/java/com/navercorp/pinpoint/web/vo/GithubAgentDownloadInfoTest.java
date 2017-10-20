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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class GithubAgentDownloadInfoTest {

    @Test
    public void defaultTest() throws Exception {
        String mockResponseString = getMockJsonString();

        ObjectMapper objectMapper = new ObjectMapper();
        JavaType agentDownloadInfoListType = objectMapper.getTypeFactory().constructCollectionType(List.class, GithubAgentDownloadInfo.class);

        List<GithubAgentDownloadInfo> agentDownloadInfoList = objectMapper.readValue(mockResponseString, agentDownloadInfoListType);

        Assert.assertEquals(15, agentDownloadInfoList.size());
    }

    private String getMockJsonString() throws IOException {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("mock/github_pinpoint_release_response.json");
            return IOUtils.toString(resourceAsStream);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }

}

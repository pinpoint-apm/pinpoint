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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import com.navercorp.pinpoint.web.install.model.AgentInstallationInfo;
import com.navercorp.pinpoint.web.install.model.GithubAgentDownloadInfo;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Taejin Koo
 */
public class AgentInstallationInfoTest {

    @Test
    public void testName() throws Exception {
        AgentInstallationInfo agentInstallInfo = new AgentInstallationInfo(new GithubAgentDownloadInfo("1.0.0", "downloadUrl"));

        ObjectMapper objectMapper = Jackson.newMapper();
        String jsonString = objectMapper.writeValueAsString(agentInstallInfo);
        Map<String, Object> map = objectMapper.readValue(jsonString, TypeRef.map());

        assertThat(map)
                .containsKey("version")
                .containsKey("downloadUrl")
                .containsKey("installationArgument");
    }

}

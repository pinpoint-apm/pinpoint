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

package com.navercorp.pinpoint.web.vo.install;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author Taejin Koo
 */
public class AgentInstallationInfoTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testName() throws Exception {
        AgentInstallationInfo agentInstallInfo = new AgentInstallationInfo(new GithubAgentDownloadInfo("1.0.0", "downloadUrl"));

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(agentInstallInfo);
        Map<String, Object> map = objectMapper.readValue(jsonString, TypeRef.map());

        logger.debug(map);

        Assertions.assertTrue(map.containsKey("version"));
        Assertions.assertTrue(map.containsKey("downloadUrl"));
        Assertions.assertTrue(map.containsKey("installationArgument"));
    }

}

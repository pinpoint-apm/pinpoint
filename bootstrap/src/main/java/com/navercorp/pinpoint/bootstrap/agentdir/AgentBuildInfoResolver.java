/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.agentdir;

import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author yjqg6666
 */
public class AgentBuildInfoResolver {

    private final BootLogger logger = BootLogger.getLogger(this.getClass().getName());

    public static final String INFO_FILE_NAME = "build.info";

    private final AgentDirectory agentDirectory;

    public AgentBuildInfoResolver(AgentDirectory agentDirectory) {
        this.agentDirectory = agentDirectory;
    }

    public AgentBuildInfo resolve() {
        File infoFile = new File(agentDirectory.getAgentDirPath() + File.separator + INFO_FILE_NAME);
        boolean available = infoFile.exists() && infoFile.canRead();
        if (!available) {
            return AgentBuildInfo.notAvailable();
        }
        Properties properties = new Properties();
        try {
            FileReader fileReader = new FileReader(infoFile);
            properties.load(fileReader);
        } catch (FileNotFoundException e) {
            //should not be here
            logger.warn(infoFile + " not found", e);
            return AgentBuildInfo.notAvailable();
        } catch (IOException e) {
            logger.warn("load build info file error", e);
            return AgentBuildInfo.notAvailable();
        }
        final String commitIdAbbr = properties.getProperty("git.commit.id.abbrev", "");
        if (StringUtils.isEmpty(commitIdAbbr)) {
            return AgentBuildInfo.notAvailable();
        }
        return new AgentBuildInfo(
                properties.getProperty("git.build.time", ""),
                properties.getProperty("git.branch", ""),
                properties.getProperty("git.build.version", ""),
                commitIdAbbr,
                properties.getProperty("git.commit.id.full", ""),
                properties.getProperty("git.tags", "")
        );
    }
}

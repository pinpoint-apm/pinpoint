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

/**
 * @author yjqg6666
 */
public class AgentBuildInfo {

    public static final int INFO_TYPE_NA = 0;

    public static final int INFO_TYPE_FOUND = 1;

    public static final String NA = "";

    private final int type;
    private final String buildTime;
    private final String gitBranch;
    private final String gitBuildVersion;
    private final String gitCommitIdAbbrev;
    private final String gitCommitIdFull;
    private final String gitTags;

    private AgentBuildInfo() {
        this.type = INFO_TYPE_NA;
        this.buildTime = NA;
        this.gitBranch = NA;
        this.gitBuildVersion = NA;
        this.gitCommitIdAbbrev = NA;
        this.gitCommitIdFull = NA;
        this.gitTags = NA;
    }

    public static AgentBuildInfo notAvailable() {
        return new AgentBuildInfo();
    }

    public AgentBuildInfo(String buildTime, String gitBranch, String gitBuildVersion, String gitCommitIdAbbrev, String gitCommitIdFull, String gitTags) {
        this.type = INFO_TYPE_FOUND;
        this.buildTime = buildTime != null ? buildTime : NA;
        this.gitBranch = gitBranch != null ? gitBranch : NA;
        this.gitBuildVersion = gitBuildVersion != null ? gitBuildVersion : NA;
        this.gitCommitIdAbbrev = gitCommitIdAbbrev != null ? gitCommitIdAbbrev : NA;
        this.gitCommitIdFull = gitCommitIdFull != null ? gitCommitIdFull : NA;
        this.gitTags = gitTags != null ? gitTags : NA;
    }

    public int getType() {
        return type;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public String getGitBuildVersion() {
        return gitBuildVersion;
    }

    public String getGitCommitIdAbbrev() {
        return gitCommitIdAbbrev;
    }

    public String getGitCommitIdFull() {
        return gitCommitIdFull;
    }

    public String getGitTags() {
        return gitTags;
    }

    public boolean isAvailable() {
        return type == INFO_TYPE_FOUND;
    }
}

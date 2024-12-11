/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.test.plugin;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Jongho Moon
 */
public class PluginTestContext {

    private final Path agentJar;
    private final String profile;
    private final Path configFile;
    private final Path logLocationConfig;
    private final List<String> repositoryUrls;
    private final Class<?> testClass;
    private final Path testClassLocation;

    private final List<String> jvmArguments;
    private final boolean debug;

    private final List<String> importPluginIds;
    private final List<String> transformIncludeList;
    private final List<String> agentLibList;
    private final List<String> sharedLibList;
    private final List<String> junitLibList;
    private final boolean manageTraceObject;

    public PluginTestContext(Path agentJar, String profile, Path configFile, Path logLocationConfig,
                             List<String> repositoryUrls,
                             Class<?> testClass, Path testClassLocation, List<String> jvmArguments,
                             boolean debug,
                             List<String> importPluginIds, boolean manageTraceObject, List<String> transformIncludeList, List<String> agentLibList, List<String> sharedLibList, List<String> junitLibList) {
        this.agentJar = agentJar;
        this.profile = profile;
        this.configFile = configFile;
        this.logLocationConfig = logLocationConfig;
        this.repositoryUrls = repositoryUrls;
        this.testClass = testClass;
        this.testClassLocation = testClassLocation;
        this.jvmArguments = jvmArguments;
        this.debug = debug;
        this.importPluginIds = importPluginIds;
        this.manageTraceObject = manageTraceObject;
        this.transformIncludeList = transformIncludeList;
        this.agentLibList = agentLibList;
        this.sharedLibList = sharedLibList;
        this.junitLibList = junitLibList;
    }

    public List<String> getRepositoryUrls() {
        return repositoryUrls;
    }

    public Path getTestClassLocation() {
        return testClassLocation;
    }

    public Path getAgentJar() {
        return agentJar;
    }

    public String getProfile() {
        return profile;
    }

    public Path getConfigFile() {
        return configFile;
    }

    public Path getLogLocationConfig() {
        return logLocationConfig;
    }

    public List<String> getJvmArguments() {
        return jvmArguments;
    }

    public boolean isDebug() {
        return debug;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public List<String> getImportPluginIds() {
        return importPluginIds;
    }

    public boolean isManageTraceObject() {
        return manageTraceObject;
    }

    public List<String> getTransformIncludeList() {
        return transformIncludeList;
    }

    public List<String> getAgentLibList() {
        return agentLibList;
    }

    public List<String> getSharedLibList() {
        return sharedLibList;
    }

    public List<String> getJunitLibList() {
        return junitLibList;
    }
}

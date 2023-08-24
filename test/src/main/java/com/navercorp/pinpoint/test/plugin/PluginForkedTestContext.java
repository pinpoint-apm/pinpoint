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

import java.util.List;

/**
 * @author Jongho Moon
 */
public class PluginForkedTestContext {

    private final String agentJar;
    private final String profile;
    private final String configFile;
    private final String logLocationConfig;

    private final List<String> requiredLibraries;
    private final List<String> mavenDependencyLibraries;
    private final List<String> repositoryUrls;
    private final Class<?> testClass;
    private final String testClassLocation;

    private final List<String> jvmArguments;
    private final boolean debug;

    private final int jvmVersion;
    private final String javaExecutable;

    private final List<String> importPluginIds;
    private final List<String> pluginLibList;
    private final List<String> transformIncludeList;
    private final boolean manageTraceObject;

    public PluginForkedTestContext(String agentJar, String profile, String configFile, String logLocationConfig,
                                   List<String> requiredLibraries, List<String> mavenDependencyLibraries, List<String> repositoryUrls,
                                   Class<?> testClass, String testClassLocation, List<String> jvmArguments,
                                   boolean debug, List<String> importPluginIds, List<String> pluginLibList, boolean manageTraceObject, List<String> transformIncludeList) {
        this(agentJar, profile, configFile, logLocationConfig, requiredLibraries, mavenDependencyLibraries, repositoryUrls, testClass, testClassLocation, jvmArguments, debug, -1, "", importPluginIds, pluginLibList, manageTraceObject, transformIncludeList);
    }

    public PluginForkedTestContext(String agentJar, String profile, String configFile, String logLocationConfig,
                                   List<String> requiredLibraries, List<String> mavenDependencyLibraries, List<String> repositoryUrls,
                                   Class<?> testClass, String testClassLocation, List<String> jvmArguments,
                                   boolean debug, int jvmVersion,
                                   String javaExecutable, List<String> importPluginIds, List<String> pluginLibList, boolean manageTraceObject, List<String> transformIncludeList) {
        this.agentJar = agentJar;
        this.profile = profile;
        this.configFile = configFile;
        this.logLocationConfig = logLocationConfig;
        this.requiredLibraries = requiredLibraries;
        this.mavenDependencyLibraries = mavenDependencyLibraries;
        this.repositoryUrls = repositoryUrls;
        this.testClass = testClass;
        this.testClassLocation = testClassLocation;
        this.jvmArguments = jvmArguments;
        this.debug = debug;
        this.jvmVersion = jvmVersion;
        this.javaExecutable = javaExecutable;
        this.importPluginIds = importPluginIds;
        this.pluginLibList = pluginLibList;
        this.transformIncludeList = transformIncludeList;
        this.manageTraceObject = manageTraceObject;
    }

    public List<String> getRequiredLibraries() {
        return requiredLibraries;
    }

    public List<String> getMavenDependencyLibraries() {
        return mavenDependencyLibraries;
    }

    public List<String> getRepositoryUrls() {
        return repositoryUrls;
    }

    public String getTestClassLocation() {
        return testClassLocation;
    }

    public String getAgentJar() {
        return agentJar;
    }

    public String getProfile() {
        return profile;
    }

    public String getConfigFile() {
        return configFile;
    }

    public String getLogLocationConfig() {
        return logLocationConfig;
    }

    public List<String> getJvmArguments() {
        return jvmArguments;
    }

    public int getJvmVersion() {
        return jvmVersion;
    }

    public boolean isDebug() {
        return debug;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public String getJavaExecutable() {
        return javaExecutable;
    }

    public List<String> getImportPluginIds() {
        return importPluginIds;
    }

    public List<String> getPluginLibList() {
        return pluginLibList;
    }

    public boolean isManageTraceObject() {
        return manageTraceObject;
    }

    public List<String> getTransformIncludeList() {
        return transformIncludeList;
    }

    @Override
    public String toString() {
        return "PluginTestContext{" +
                "agentJar='" + agentJar + '\'' +
                ", profile='" + profile + '\'' +
                ", configFile='" + configFile + '\'' +
                ", logLocationConfig='" + logLocationConfig + '\'' +
                ", requiredLibraries=" + requiredLibraries +
                ", mavenDependencyLibraries=" + mavenDependencyLibraries +
                ", repositoryUrls=" + repositoryUrls +
                ", testClass=" + testClass +
                ", testClassLocation='" + testClassLocation + '\'' +
                ", jvmArguments=" + jvmArguments +
                ", debug=" + debug +
                ", jvmVersion=" + jvmVersion +
                ", javaExecutable='" + javaExecutable + '\'' +
                ", importPluginIds=" + importPluginIds +
                ", pluginLibList=" + pluginLibList +
                ", manageTraceObject=" + manageTraceObject +
                '}';
    }
}

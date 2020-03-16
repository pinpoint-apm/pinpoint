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

import java.util.Arrays;
import java.util.List;

/**
 * @author Jongho Moon
 */
public class PinpointPluginTestContext {

    private final String agentJar;
    private final String profile;
    private final String configFile;

    private final List<String> requiredLibraries;
    private final List<String> mavenDependencyLibraries;
    private final Class<?> testClass;
    private final String testClassLocation;

    private final String[] jvmArguments;
    private final boolean debug;

    private final int jvmVersion;
    private final String javaExecutable;

    private final List<String> importPluginIds;

    public PinpointPluginTestContext(String agentJar, String profile, String configFile,
                                     List<String> requiredLibraries, List<String> mavenDependencyLibraries,
                                     Class<?> testClass, String testClassLocation, String[] jvmArguments,
                                     boolean debug, int jvmVersion,
                                     String javaExecutable, List<String> importPluginIds) {
        this.agentJar = agentJar;
        this.profile = profile;
        this.configFile = configFile;
        this.requiredLibraries = requiredLibraries;
        this.mavenDependencyLibraries = mavenDependencyLibraries;
        this.testClass = testClass;
        this.testClassLocation = testClassLocation;
        this.jvmArguments = jvmArguments;
        this.debug = debug;
        this.jvmVersion = jvmVersion;
        this.javaExecutable = javaExecutable;
        this.importPluginIds = importPluginIds;
    }

    public List<String> getRequiredLibraries() {
        return requiredLibraries;
    }

    public List<String> getMavenDependencyLibraries() {
        return mavenDependencyLibraries;
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

    public String[] getJvmArguments() {
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

    @Override
    public String toString() {
        return "PinpointPluginTestContext{" +
                "agentJar='" + agentJar + '\'' +
                ", profile='" + profile + '\'' +
                ", configFile='" + configFile + '\'' +
                ", requiredLibraries=" + requiredLibraries +
                ", mavenDependencyLibraries=" + mavenDependencyLibraries +
                ", testClass=" + testClass +
                ", testClassLocation='" + testClassLocation + '\'' +
                ", jvmArguments=" + Arrays.toString(jvmArguments) +
                ", debug=" + debug +
                ", jvmVersion=" + jvmVersion +
                ", javaExecutable='" + javaExecutable + '\'' +
                ", importPluginIds=" + importPluginIds +
                '}';
    }

}

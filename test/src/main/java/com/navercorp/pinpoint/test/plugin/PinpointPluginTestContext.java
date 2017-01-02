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
 *
 */
public class PinpointPluginTestContext {

    private final String agentJar;
    private final String configFile;

    private final List<String> requiredLibraries;
    private final Class<?> testClass;
    private final String testClassLocation;
    
    private final String[] jvmArguments;
    private final boolean debug;
    
    private final int jvmVersion;
    private final String javaExecutable;

    
    
    public PinpointPluginTestContext(String agentJar, String configFile, List<String> requiredLibraries, Class<?> testClass, String testClassLocation, String[] jvmArguments, boolean debug, int jvmVersion, String javaExecutable) {
        this.agentJar = agentJar;
        this.configFile = configFile;
        this.requiredLibraries = requiredLibraries;
        this.testClass = testClass;
        this.testClassLocation = testClassLocation;
        this.jvmArguments = jvmArguments;
        this.debug = debug;
        this.jvmVersion = jvmVersion;
        this.javaExecutable = javaExecutable;
    }

    public List<String> getRequiredLibraries() {
        return requiredLibraries;
    }

    public String getTestClassLocation() {
        return testClassLocation;
    }

    public String getAgentJar() {
        return agentJar;
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
}

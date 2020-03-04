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


import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentDirectory {

    public static final String LIB_DIR = "lib";
    public static final String PLUGIN_DIR = "plugin";
    public static final String LOGS_DIR = "logs";
    public static final String PROFILES_DIR = "profiles";

    private final String agentJarName;
    private final String agentJarFullPath;
    private final String agentDirPath;

    private final BootDir bootDir;
    private final List<String> plugins;
    private final List<URL> libs;


    public AgentDirectory(String agentJarName,
                          String agentJarFullPath,
                          String agentDirPath,
                          BootDir bootDir,
                          List<URL> libs,
                          List<String> plugins) {

        this.agentJarName = agentJarName;
        this.agentJarFullPath = agentJarFullPath;
        this.agentDirPath = agentDirPath;

        this.bootDir = Assert.requireNonNull(bootDir, "bootDir");
        this.libs = libs;
        this.plugins = plugins;
    }

    public BootDir getBootDir() {
        return bootDir;
    }

    public List<URL> getLibs() {
        return libs;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public String getAgentJarName() {
        return this.agentJarName;
    }

    public String getAgentJarFullPath() {
        return agentJarFullPath;
    }

    public String getAgentDirPath() {
        return agentDirPath;
    }

    public String getAgentLibPath() {
        return appendAgentDirPath(LIB_DIR);
    }

    public String getAgentLogFilePath() {
        return appendAgentDirPath(LOGS_DIR);
    }

    public String getAgentPluginPath() {
        return appendAgentDirPath(PLUGIN_DIR);
    }

    public String getAgentConfigPath() {
        return appendAgentDirPath("pinpoint.config");
    }

    public String getProfilesPath() {
        return appendAgentDirPath(PROFILES_DIR);
    }

    private String appendAgentDirPath(String fileName) {
        return this.agentDirPath + File.separator + fileName;
    }

    public String[] getProfileDirs() {
        final String profilesPath = getProfilesPath();
        final File profilesDir = new File(profilesPath);
        final String[] profileDirs = profilesDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (dir.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        return defaultStringArray(profileDirs);
    }

    private String[] defaultStringArray(String[] profileDirs) {
        if (profileDirs == null) {
            return new String[0];
        }
        return profileDirs;
    }
}

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


import com.navercorp.pinpoint.bootstrap.config.Profiles;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentDirectory {

    public static final String AGENT_ROOT_PATH_KEY = "pinpoint.agent.root.path";

    public static final Path LIB_DIR = Paths.get("lib");
    public static final Path PLUGIN_DIR = Paths.get("plugin");
    public static final Path LOGS_DIR = Paths.get("logs");
    public static final Path PROFILES_DIR = Paths.get("profiles");

    private final Path agentJarName;
    private final Path agentJarFullPath;
    private final Path agentDirPath;

    private final BootDir bootDir;
    private final List<Path> plugins;
    private final List<Path> libs;


    public AgentDirectory(Path agentJarName,
                          Path agentJarFullPath,
                          Path agentDirPath,
                          BootDir bootDir,
                          List<Path> libs,
                          List<Path> plugins) {

        this.agentJarName = agentJarName;
        this.agentJarFullPath = agentJarFullPath;
        this.agentDirPath = agentDirPath;

        this.bootDir = Objects.requireNonNull(bootDir, "bootDir");
        this.libs = libs;
        this.plugins = plugins;
    }

    public BootDir getBootDir() {
        return bootDir;
    }

    public List<Path> getLibs() {
        return libs;
    }

    public List<Path> getPlugins() {
        return plugins;
    }

    public Path getAgentJarName() {
        return this.agentJarName;
    }

    public Path getAgentJarFullPath() {
        return agentJarFullPath;
    }

    public Path getAgentDirPath() {
        return agentDirPath;
    }

    public Path getAgentLibPath() {
        return appendAgentDirPath(LIB_DIR);
    }

    public Path getAgentLogFilePath() {
        return appendAgentDirPath(LOGS_DIR);
    }

    public Path getAgentPluginPath() {
        return appendAgentDirPath(PLUGIN_DIR);
    }

    public Path getAgentConfigPath() {
        return appendAgentDirPath(Paths.get(Profiles.CONFIG_FILE_NAME));
    }

    public Path getProfilesPath() {
        return appendAgentDirPath(PROFILES_DIR);
    }

    private Path appendAgentDirPath(Path fileName) {
        return this.agentDirPath.resolve(fileName);
    }

    public String[] getProfileDirs() {
        List<String> fileList = new ArrayList<>();

        final Path profilesPath = getProfilesPath();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(profilesPath)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    fileList.add(path.getFileName().toString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("profileDirs traverse error " + profilesPath, e);
        }
        return fileList.toArray(new String[0]);
    }

}

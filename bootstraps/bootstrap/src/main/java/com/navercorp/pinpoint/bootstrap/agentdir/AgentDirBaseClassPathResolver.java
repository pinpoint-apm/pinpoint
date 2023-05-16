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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * @author emeroad
 */
public class AgentDirBaseClassPathResolver implements ClassPathResolver {

    private final BootLogger logger = BootLogger.getLogger(this.getClass());

    static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((\\-SNAPSHOT)|(-RC[0-9]+)|(-p[0-9]+))?)?";

    static final JarDescription bootstrap = new JarDescription("pinpoint-bootstrap", true);
    private static final String EXTENSIONS = "*.{jar,xml,properties}";

    // boot dir
    private final JarDescription commons = new JarDescription("pinpoint-commons", true);
    private final JarDescription commonsConfig = new JarDescription("pinpoint-commons-config", true);
    private final JarDescription bootstrapCore = new JarDescription("pinpoint-bootstrap-core", true);
    private final JarDescription annotations = new JarDescription("pinpoint-annotations", false);
    private final JarDescription bootstrapJava8 = new JarDescription("pinpoint-bootstrap-java8", false);
    private final JarDescription bootstrapJava9 = new JarDescription("pinpoint-bootstrap-java9", false);
    private final JarDescription bootstrapJava9internal = new JarDescription("pinpoint-bootstrap-java9-internal", false);
    private final JarDescription bootstrapJava15 = new JarDescription("pinpoint-bootstrap-java15", false);
    private final JarDescription bootstrapJava16 = new JarDescription("pinpoint-bootstrap-java16", false);
    private final List<JarDescription> bootJarDescriptions = Arrays.asList(commons, commonsConfig, bootstrapCore, annotations, bootstrapJava8, bootstrapJava9, bootstrapJava9internal, bootstrapJava15, bootstrapJava16);

    private final Path bootstrapJarPath;



    public AgentDirBaseClassPathResolver(Path bootstrapJarPath) {
        this.bootstrapJarPath = Objects.requireNonNull(bootstrapJarPath, "classPath");
    }


    @Override
    public AgentDirectory resolve() {
        if (!bootstrapJarPath.toFile().isFile()) {
            throw new IllegalStateException(bootstrapJarPath + " not found");
        }

        // find bootstrap.jar
        final Path bootstrapJarName = this.findBootstrapJar(this.bootstrapJarPath);
        if (bootstrapJarName == null) {
            throw new IllegalStateException(bootstrap.getSimplePattern() + " not found.");
        }

        final Path agentDirPath = getAgentDirPath(this.bootstrapJarPath);

        final BootDir bootDir = resolveBootDir(agentDirPath);

        final Path agentLibPath = getAgentLibPath(agentDirPath);
        final List<Path> libs = resolveLib(agentLibPath);

        Path agentPluginPath = getAgentPluginPath(agentDirPath);
        final List<Path> plugins = resolvePlugins(agentPluginPath);

        final AgentDirectory agentDirectory =
                new AgentDirectory(bootstrapJarName, this.bootstrapJarPath, agentDirPath, bootDir, libs, plugins);

        return agentDirectory;
    }

    private Path getAgentDirPath(Path agentJarFullPath) {
        Path agentDirPathStr = agentJarFullPath.getParent();
        if (agentDirPathStr == null) {
            throw new IllegalStateException("agentDirPath is null " + agentJarFullPath);
        }

        logger.info("Agent original-path:" + agentDirPathStr);
        // defense alias change

        Path agentDirPath = FileUtils.toRealPath(agentDirPathStr);
        logger.info("Agent real-path:" + agentDirPath);
        return agentDirPath;
    }


    private BootDir resolveBootDir(Path agentDirPath) {
        Path bootDirPath = agentDirPath.resolve("boot");
        return new BootDir(bootDirPath, bootJarDescriptions);
    }

    public List<JarDescription> getBootJarDescriptions() {
        return Collections.unmodifiableList(bootJarDescriptions);
    }

    Path findBootstrapJar(Path bootstrapJarPath) {
        final Path fileName = bootstrapJarPath.getFileName();
        if(fileName == null) {
            return null;
        }
        final Matcher matcher = bootstrap.getVersionPattern().matcher(fileName.toString());
        if (!matcher.find()) {
            return null;
        }
        return fileName;
    }

    private Path getAgentLibPath(Path agentDirPath) {
        return agentDirPath.resolve("lib");
    }

    private Path getAgentPluginPath(Path agentDirPath) {
        return agentDirPath.resolve("plugin");
    }

    private List<Path> resolveLib(Path agentLibPath) {

        if (checkDirectory(agentLibPath.toFile())) {
            return Collections.emptyList();
        }

        final List<Path> libFileList = FileUtils.listFiles(agentLibPath, EXTENSIONS);
        if (libFileList.isEmpty()) {
            throw new RuntimeException(agentLibPath + " lib dir is empty");
        }
        // add directory
        libFileList.add(agentLibPath);

        return libFileList;
    }

    private List<Path> resolvePlugins(Path agentPluginPath) {

        if (checkDirectory(agentPluginPath.toFile())) {
            logger.warn(agentPluginPath + " is not a directory");
            return Collections.emptyList();
        }

        final List<Path> jars = FileUtils.listFiles(agentPluginPath, "*.jar");
        if (jars.isEmpty()) {
            return Collections.emptyList();
        }

        List<Path> pluginFileList = filterReadPermission(jars);
        for (Path pluginJar : pluginFileList) {
            logger.info("Found plugins:" + pluginJar);
        }
        return pluginFileList;
    }

    private boolean checkDirectory(File file) {
        if (!file.exists()) {
            logger.warn(file + " not found");
            return true;
        }
        if (!file.isDirectory()) {
            logger.warn(file + " is not a directory");
            return true;
        }
        return false;
    }

    private List<Path> filterReadPermission(List<Path> jars) {
        List<Path> result = new ArrayList<>();
        for (Path pluginJar : jars) {
            if (!pluginJar.toFile().canRead()) {
                logger.info("File '" + pluginJar + "' cannot be read");
                continue;
            }

            result.add(pluginJar);
        }
        return result;
    }


}

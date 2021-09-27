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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootDir {

    private final BootLogger logger = BootLogger.getLogger(this.getClass());

    private final List<Path> jars;

    public BootDir(Path baseDir, List<JarDescription> jarDescriptions) {
        Objects.requireNonNull(baseDir, "baseDir");
        Objects.requireNonNull(jarDescriptions, "jarDescriptions");
        this.jars = verify(baseDir, jarDescriptions);
    }

    private List<Path> verify(Path baseDir, List<JarDescription> jarDescriptions) {
        final List<Path> jarFiles = FileUtils.listFiles(baseDir, "*.jar");
        if (jarFiles.isEmpty()) {
            logger.info(baseDir + " is empty");
            return null;
        }

        List<Path> resolvedJarList = new ArrayList<>(jarDescriptions.size());
        for (JarDescription jarDescription : jarDescriptions) {
            final Path jarFileName = find(jarFiles, jarDescription);
            if (jarFileName == null) {
                final String errorMessage = jarDescription.getSimplePattern() + " not found";
                if (jarDescription.isRequired()) {
                    throw new IllegalStateException(errorMessage);
                }
            } else {
                resolvedJarList.add(jarFileName.toAbsolutePath());
            }
        }
        return resolvedJarList;
    }

    private Path find(List<Path> jarFiles, final JarDescription jarDescription) {
        final String jarName = jarDescription.getJarName();
        final Pattern pattern = jarDescription.getVersionPattern();

        final List<Path> jarPathList = findFileByPattern(jarFiles, pattern);
        if (jarPathList.isEmpty()) {
            logger.info(jarName + " not found.");
            return null;
        }
        if (jarPathList.size() == 1) {
            final Path file = jarPathList.get(0);
            logger.info("found " + jarName + " path:" + file);
            return FileUtils.toRealPath(file);
        }

        logger.warn("too many " + jarName + " found. " + jarPathList);
        return null;
    }

    private List<Path> findFileByPattern(List<Path> jarFiles, Pattern pattern) {
        List<Path> findList = new ArrayList<>();
        for (Path jarFile : jarFiles) {
            String fileName = jarFile.getFileName().toString();
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches()) {
                findList.add(jarFile);
            }
        }
        return findList;
    }

    public List<Path> getJarPath() {
        return jars;
    }


    public List<JarFile> openJarFiles() {
        final List<JarFile> jarFileList = new ArrayList<>(jars.size());
        for (Path jarPath : jars) {
            final JarFile jarFile = JarFileUtils.openJarFile(jarPath.toFile());
            jarFileList.add(jarFile);
        }
        return jarFileList;
    }

}

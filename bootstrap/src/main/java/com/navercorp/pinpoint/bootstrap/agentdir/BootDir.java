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
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootDir {

    private final BootLogger logger = BootLogger.getLogger(this.getClass().getName());

    private final File baseDirFile;
    private final List<String> jars;

    public BootDir(String baseDir, List<JarDescription> jarDescriptions) {
        Assert.requireNonNull(baseDir, "baseDir");
        Assert.requireNonNull(jarDescriptions, "jarDescriptions");
        this.baseDirFile = new File(baseDir);
        this.jars = verify(baseDirFile, jarDescriptions);
    }

    private List<String> verify(File baseDirFile, List<JarDescription> jarDescriptions) {
        final String[] jarExtensions = {".jar"};
        final File[] jarFiles = FileUtils.listFiles(baseDirFile, jarExtensions);
        if (FileUtils.isEmpty(jarFiles)) {
            logger.info(baseDirFile.getName() + " is empty");
            return null;
        }

        List<String> resolvedJarList = new ArrayList<String>(jarDescriptions.size());
        for (JarDescription jarDescription : jarDescriptions) {
            final String jarFileName = find(jarFiles, jarDescription);
            if (jarFileName == null) {
                final String errorMessage = jarDescription.getSimplePattern() + " not found";
                if (jarDescription.isRequired()) {
                    throw new IllegalStateException(errorMessage);
                }
            } else {
                resolvedJarList.add(jarFileName);
            }
        }
        return resolvedJarList;
    }

    private String find(File[] jarFiles, final JarDescription jarDescription) {
        final String jarName = jarDescription.getJarName();
        final Pattern pattern = jarDescription.getVersionPattern();

        final List<File> jarFIleList = findFileByPattern(jarFiles, pattern);
        if (jarFIleList.isEmpty()) {
            logger.info(jarName + " not found.");
            return null;
        }
        if (jarFIleList.size() == 1) {
            final File file = jarFIleList.get(0);
            logger.info("found " + jarName + " path:" + file);
            return FileUtils.toCanonicalPath(file);
        }

        logger.warn("too many " + jarName + " found. " + jarFIleList);
        return null;
    }

    private List<File> findFileByPattern(File[] jarFiles, Pattern pattern) {
        List<File> findList = new ArrayList<File>();
        for (File jarFile : jarFiles) {
            Matcher matcher = pattern.matcher(jarFile.getName());
            if (matcher.matches()) {
                findList.add(jarFile);
            }
        }
        return findList;
    }

    public List<String> toList() {
        return new ArrayList<String>(jars);
    }


    public List<JarFile> openJarFiles() {
        final List<JarFile> jarFileList = new ArrayList<JarFile>(jars.size());
        for (String jarPath : jars) {
            final JarFile jarFile = JarFileUtils.openJarFile(jarPath);
            jarFileList.add(jarFile);
        }
        return jarFileList;
    }

}

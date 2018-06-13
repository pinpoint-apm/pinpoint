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

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BootDir {

    private final BootLogger logger = BootLogger.getLogger(this.getClass().getName());

    private final String commons;
    private final String bootstrapCore;
    private final String bootstrapCoreOptional;
    private final String bootstrapJava9;
    private final String annotations;


    public BootDir(String commons, String bootstrapCore, String bootstrapCoreOptional, String bootstrapJava9, String annotations) {
        if (commons == null) {
            throw new NullPointerException("commons must not be null");
        }
        if (bootstrapCore == null) {
            throw new NullPointerException("bootstrapCore must not be null");
        }

        this.commons = commons;
        this.bootstrapCore = bootstrapCore;
        // optional
        this.bootstrapCoreOptional = bootstrapCoreOptional;
        // optional
        this.bootstrapJava9 = bootstrapJava9;
        // optional
        this.annotations = annotations;

        verify();
    }

    private void verify() {
        // 2st find pinpoint-commons.jar
        final String pinpointCommonsJar = getCommons();
        if (pinpointCommonsJar == null) {
            throw new IllegalStateException("pinpoint-commons-x.x.x(-SNAPSHOT).jar not found");
        }

        // 3st find bootstrap-core.jar
        final String bootStrapCoreJar = getBootstrapCore();
        if (bootStrapCoreJar == null) {
            throw new IllegalStateException("pinpoint-bootstrap-core-x.x.x(-SNAPSHOT).jar not found");
        }

        // 4th find bootstrap-core-optional.jar
        final String bootStrapCoreOptionalJar = getBootstrapCoreOptional();
        if (bootStrapCoreOptionalJar == null) {
            // optional
            logger.info("pinpoint-bootstrap-core-optional-x.x.x(-SNAPSHOT).jar not found");
        }

        final String bootStrapJava9Jar = getBootstrapJava9();
        if (bootStrapJava9Jar == null) {
            // optional
            logger.info("pinpoint-bootstrap-java9-x.x.x(-SNAPSHOT).jar not found");
        }
        // 6th find annotations.jar : optional dependency
        final String annotationsJar = getAnnotations();
        if (annotationsJar == null) {
            logger.info("pinpoint-annotations-x.x.x(-SNAPSHOT).jar not found");
        }
    }

    public String getCommons() {
        return commons;
    }

    public String getBootstrapCore() {
        return bootstrapCore;
    }

    public String getBootstrapCoreOptional() {
        return bootstrapCoreOptional;
    }

    public String getBootstrapJava9() {
        return bootstrapJava9;
    }

    public String getAnnotations() {
        return annotations;
    }

    public List<String> toList() {
        final List<String> list = new ArrayList<String>();

        addFilePath(list, commons, true);
        addFilePath(list, bootstrapCore, true);
        addFilePath(list, bootstrapCoreOptional, false);
        addFilePath(list, bootstrapJava9, false);
        addFilePath(list, annotations, false);

        return list;
    }

    private void addFilePath(List<String> list, String filePath, boolean required) {
        if (required) {
            if (filePath == null) {
                throw new IllegalStateException("filePath must not be null");
            }
        } else {
            if (filePath == null) {
                return;
            }
        }
        list.add(filePath);
    }

    public List<JarFile> openJarFiles() {
        final List<JarFile> jarFileList = new ArrayList<JarFile>();

        addJarFile(jarFileList, commons, true);
        addJarFile(jarFileList, bootstrapCore, true);
        addJarFile(jarFileList, bootstrapCoreOptional, false);
        addJarFile(jarFileList, bootstrapJava9, false);
        addJarFile(jarFileList, annotations, false);

        return jarFileList;
    }

    private void addJarFile(List<JarFile> list, String filePath, boolean required) {
        if (required) {
            if (filePath == null) {
                throw new IllegalStateException("filePath must not be null");
            }
        } else {
            if (filePath == null) {
                return;
            }
        }
        JarFile jarFile = JarFileUtils.openJarFile(filePath);
        list.add(jarFile);
    }
}

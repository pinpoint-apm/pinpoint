/*
 * Copyright 2014 NAVER Corp.
 *
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

package com.navercorp.pinpoint.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

import java.net.URL;

/**
 * @author emeroad
 */
public class ProjectPathResolver {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // maven base path resolver
    // extract interface ????
    public ProjectPath resolvePathFromTestClass(Class testClass) {
        if (testClass == null) {
            throw new NullPointerException("testClass must not be null");
        }
        String testClassDir = getTestClassPath(testClass);
        // /D:/pinpoint_project/pinpoint/profiler/target/test-classes/
        String targetPath = getTargetPath(testClassDir);
        String modulePath = getModulePath(targetPath);
        String projectPath = getProjectPath(modulePath);
        String pinpointAgentPath = getPinpointAgentPath(projectPath);
        return new ProjectPath(testClassDir, targetPath, modulePath, projectPath, pinpointAgentPath);
    }

    private String getTestClassPath(Class<?> testClass) {
        logger.debug("testClass:{}", testClass);

        final ClassLoader classLoader = getDefaultClassLoader(testClass);
        final String testClassName = JavaAssistUtils.javaNameToJvmName(testClass.getName()) + ".class";
        final URL testClassResource = classLoader.getResource(testClassName);
        if (testClassResource == null) {
            throw new IllegalArgumentException("testClassName not found." + testClassName);
        }
        logger.debug("url TestClass={}", testClassResource);

        final String testClassPath = testClassResource.getPath();
        final int classClassDirFind = testClassPath.indexOf("/" + testClassName);
        if (classClassDirFind == -1) {
            throw new IllegalArgumentException(testClassName + "not found.");
        }
        final String testClassDir = testClassPath.substring(1, classClassDirFind);
        logger.debug("testClassDir:{}", testClassDir);
        return testClassDir;
    }



    private String getProjectPath(String modulePath) {
        if (modulePath == null) {
            throw new NullPointerException("modulePath must not be null");
        }
        // remove last /
        final int projectPathFound = modulePath.lastIndexOf('/');
        if (projectPathFound == -1) {
            throw new RuntimeException("projectPathFound not found");
        }
        String projectPath = modulePath.substring(0, projectPathFound);
        logger.debug("projectPath:{}", projectPath);
        return projectPath;
    }

    private String getModulePath(String targetPath) {
        final int targetFound = targetPath.lastIndexOf("target");
        if (targetFound == -1) {
            throw new RuntimeException("target not found "+ targetPath);
        }
        String modulePath = targetPath.substring(0, targetFound - 1);
        logger.debug("modulePath:{}", modulePath);
        return modulePath;
    }

    private String getPinpointAgentPath(String projectPath) {
        if (projectPath == null) {
            throw new NullPointerException("projectPath must not be null");
        }

        String pinpointAgentDir = projectPath + "/profiler/target/pinpoint-agent";
        logger.debug("pinpointAgentDir:{}", pinpointAgentDir);
        return pinpointAgentDir;

    }


    private String getTargetPath(String testClassDir) {
        if (testClassDir == null) {
            throw new NullPointerException("testClassDir must not be null");
        }
        // remove last '/'
        // D:/pinpoint_project/pinpoint/profiler/target/test-classes/ -> D:/pinpoint_project/pinpoint/profiler/target/test-classes

        final String target = "target";
        int targetFound = testClassDir.lastIndexOf(target);
        if (targetFound == -1) {
            throw new NullPointerException("targetDir not found.");
        }

        String targetDir = testClassDir.substring(0, targetFound + target.length());
        logger.debug("targetDir:{}", targetDir);
        return targetDir;
    }



    private ClassLoader getDefaultClassLoader(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        return getClassLoader(classLoader);
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }


    public static class ProjectPath {
        private final String testClassPath;
        private final String targetPath;
        private final String modulePath;
        private final String projectPath;
        private final String pinpointAgentPath;

        public ProjectPath(String testClassPath,String targetPath, String modulePath, String projectPath, String pinpointAgentPath) {
            this.testClassPath = testClassPath;
            this.targetPath = targetPath;
            this.modulePath = modulePath;
            this.projectPath = projectPath;
            this.pinpointAgentPath = pinpointAgentPath;
        }

        public String getTestClassPath() {
            return testClassPath;
        }

        public String getTargetPath() {
            return targetPath;
        }

        public String getModulePath() {
            return modulePath;
        }

        public String getProjectPath() {
            return projectPath;
        }

        public  String getPinpointAgentPath() {
            return pinpointAgentPath;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ProjectPath{");
            sb.append("testClassPath='").append(testClassPath).append('\'');
            sb.append(", targetPath='").append(targetPath).append('\'');
            sb.append(", modulePath='").append(modulePath).append('\'');
            sb.append(", projectPath='").append(projectPath).append('\'');
            sb.append(", pinpointAgentPath='").append(pinpointAgentPath).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}

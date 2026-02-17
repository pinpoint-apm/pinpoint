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

package com.navercorp.pinpoint.bootstrap;

import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirBaseClassPathResolver;
import com.navercorp.pinpoint.bootstrap.agentdir.AgentDirectory;
import com.navercorp.pinpoint.bootstrap.agentdir.BootDir;
import com.navercorp.pinpoint.bootstrap.agentdir.ClassPathResolver;
import com.navercorp.pinpoint.bootstrap.agentdir.FileUtils;
import com.navercorp.pinpoint.bootstrap.agentdir.JavaAgentPathResolver;
import com.navercorp.pinpoint.bootstrap.config.DisableOptions;

import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;

/**
 * @author emeroad
 * @author netspider
 */
public class PinpointBootStrap {

    private static final BootLogger logger = BootLogger.getLogger(PinpointBootStrap.class);

    private static final LoadState STATE = new LoadState();

    public static void premain(String agentArgs, Instrumentation instrumentation) {

        if (DisableOptions.isBootDisabled()) {
            if (logger.isWarnEnabled()) {
                logger.warn("PinPoint is disabled via Env/Property.");
            }
            return;
        }

        final boolean success = STATE.start();
        if (!success) {
            logger.warn("pinpoint-bootstrap already started. skipping agent loading.");
            return;
        }

        PinpointBootStrap bootStrap = new PinpointBootStrap(agentArgs, instrumentation);
        bootStrap.start();

    }

    private final String agentArgs;
    private final Instrumentation instrumentation;

    private PinpointBootStrap(String agentArgs, Instrumentation instrumentation) {
        this.agentArgs = agentArgs;
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation");
    }


    private void start() {
        logger.info("pinpoint agentArgs:" + agentArgs);
        logger.info("PinpointBootStrap.ClassLoader:" + PinpointBootStrap.class.getClassLoader());
        logger.info("ContextClassLoader:" + Thread.currentThread().getContextClassLoader());

        final JavaAgentPathResolver javaAgentPathResolver = JavaAgentPathResolver.newJavaAgentPathResolver();
        final Path agentPath = javaAgentPathResolver.resolveJavaAgentPath();
        logger.info("JavaAgentPath:" + agentPath);
        if (!Files.exists(agentPath)) {
            logger.warn("AgentPath not found path:" + agentPath);
        }

        if (Object.class.getClassLoader() != PinpointBootStrap.class.getClassLoader()) {
            // TODO bug : location is null
            logger.warn("Invalid pinpoint-bootstrap.jar:" + agentArgs);
            return;
        }

        final Map<String, String> agentArgsMap = argsToMap(agentArgs);

        final ClassPathResolver classPathResolver = new AgentDirBaseClassPathResolver(agentPath);

        final AgentDirectory agentDirectory = resolveAgentDir(classPathResolver);
        if (agentDirectory == null) {
            logger.warn("Agent Directory Verify fail. skipping agent loading.");
            logPinpointAgentLoadFail();
            return;
        }
        BootDir bootDir = agentDirectory.getBootDir();
        appendToBootstrapClassLoader(instrumentation, bootDir);

        ClassLoader parentClassLoader = getParentClassLoader();
        PinpointStarter bootStrap = new PinpointStarter(instrumentation, parentClassLoader, agentArgsMap, agentDirectory);
        if (!bootStrap.start()) {
            logPinpointAgentLoadFail();
        }
    }

    private AgentDirectory resolveAgentDir(ClassPathResolver classPathResolver) {
        try {
            return classPathResolver.resolve();
        } catch (Exception e) {
            logger.warn("AgentDir resolve fail Caused by:" + e.getMessage(), e);
            return null;
        }
    }


    private ClassLoader getParentClassLoader() {
        final ClassLoader classLoader = getPinpointBootStrapClassLoader();
        if (classLoader == Object.class.getClassLoader()) {
            logger.info("parentClassLoader:BootStrapClassLoader:" + classLoader);
        } else {
            logger.info("parentClassLoader:" + classLoader);
        }
        return classLoader;
    }

    private ClassLoader getPinpointBootStrapClassLoader() {
        return PinpointBootStrap.class.getClassLoader();
    }


    private Map<String, String> argsToMap(String agentArgs) {
        ArgsParser argsParser = new ArgsParser();
        Map<String, String> agentArgsMap = argsParser.parse(agentArgs);
        if (!agentArgsMap.isEmpty()) {
            logger.info("agentParameter:" + agentArgs);
        }
        return agentArgsMap;
    }

    private void appendToBootstrapClassLoader(Instrumentation instrumentation, BootDir bootDir) {
        List<JarFile> jarFiles = bootDir.openJarFiles();
        try {
            for (JarFile jarFile : jarFiles) {
                Path path = FileUtils.subpathAfterLast(Paths.get(jarFile.getName()), 2);
                logger.info("appendToBootstrapClassLoader:" + path);
                instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
            }
        } finally {
            for (JarFile jarFile : jarFiles) {
                try {
                    jarFile.close();
                } catch (Exception e) {
                    logger.warn("Failed to close JarFile:" + jarFile.getName(), e);
                }
            }
        }
    }


    private static void logPinpointAgentLoadFail() {
        final String errorLog =
                "*****************************************************************************\n" +
                        "* Pinpoint Agent load failure\n" +
                        "*****************************************************************************";
        System.err.println(errorLog);
    }


}

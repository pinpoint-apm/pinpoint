/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.test.plugin.PluginTestConstants;
import com.navercorp.pinpoint.test.plugin.PluginTestContext;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestInstance;
import com.navercorp.pinpoint.test.plugin.ProcessManager;
import com.navercorp.pinpoint.test.plugin.util.Assert;
import com.navercorp.pinpoint.test.plugin.util.CollectionUtils;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import com.navercorp.pinpoint.test.plugin.util.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.navercorp.pinpoint.test.plugin.util.SystemPropertyBuilder.format;

/**
 * @author Taejin Koo
 */
public class SharedProcessManager implements ProcessManager {
    public static final String PATH_SEPARATOR = File.pathSeparator;

    private final TaggedLogger logger = TestLogger.getLogger();

    private final PluginTestContext context;
    private final Map<String, List<Artifact>> testRepository = new LinkedHashMap<String, List<Artifact>>();

    private Process process = null;

    public SharedProcessManager(PluginTestContext context) {
        this.context = Assert.requireNonNull(context, "context");
    }

    @Override
    public Process create(PinpointPluginTestInstance pluginTestInstance) {
        return get();
    }

    @Override
    public Process get() {
        if (process == null) {
            try {
                this.process = fork();
            } catch (IOException e) {
                logger.error(e, "process fork failed");
            }
//            this.process = process;
        }
        return process;
    }

    @Override
    public void stop() {
        if (testRepository.isEmpty()) {
            if (process != null) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        process.destroy();
                    }

                }, 10 * 1000);

                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    // ignore
                }

                timer.cancel();
                process = null;
            }
        }
    }

    public Process fork() throws IOException {
        File workingDirectory = new File(".");
        String[] commands = buildCommand();

        ProcessBuilder builder = new ProcessBuilder();

        builder.command(commands);
        builder.redirectErrorStream(true);
        builder.directory(workingDirectory);

        logger.info("Working directory: {}", System.getProperty("user.dir"));
        logger.info("Command: {}", builder.command());
        logger.info("CommandSize: {}", builder.command().toString().length());

        this.process = builder.start();
        return process;
    }

    public boolean registerTest(String testId, List<Artifact> artifactList) {
        if (testRepository.containsKey(testId)) {
            return false;
        }

        testRepository.put(testId, artifactList);
        return true;
    }

    public boolean deregisterTest(String testId) {
        List<Artifact> value = testRepository.remove(testId);
        return value != null;
    }

    public Process getProcess() {
        return process;
    }

    public boolean isStarted() {
        return process != null;
    }

    private String[] buildCommand() {
        List<String> list = new ArrayList<String>();

        list.add(context.getJavaExecutable());

        list.add("-Xmx1024m");
        list.add("-XX:MaxPermSize=512m");

        String classPath = join(context.getRequiredLibraries());
        list.add("-cp");
        list.add(classPath);

        list.add(getAgent());
        list.add(format("pinpoint.agentId", "build.test.0"));
        list.add(format("pinpoint.applicationName", "test"));
        list.add(format("java.net.preferIPv4Addresses", "true"));

        final String mavenDependencyResolverClassPaths = join(context.getMavenDependencyLibraries());
        list.add(format(SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS, mavenDependencyResolverClassPaths));
        list.add(format(SharedPluginTestConstants.TEST_LOCATION,  context.getTestClassLocation()));
        list.add(format(SharedPluginTestConstants.TEST_CLAZZ_NAME, context.getTestClass().getName()));

//        list.add("-D" + PINPOINT_TEST_ID + "=" + testCase.getTestId());

        for (String arg : context.getJvmArguments()) {
            list.add(arg);
        }

        if (context.isDebug()) {
            list.addAll(getDebugOptions());
        }

        if (context.getProfile() != null) {
            list.add(format("pinpoint.profiler.profiles.active", context.getProfile()));
        }

        if (context.getConfigFile() != null) {
            list.add(format("pinpoint.config", context.getConfigFile()));
            list.add(format("pinpoint.config.load.mode", "simple"));
        }

        for (String arg : getVmArgs()) {
            list.add(arg);
        }

        String mainClass = getMainClass();

        if (mainClass.endsWith(".jar")) {
            list.add("-jar");
        }

        list.add(mainClass);

        Set<Map.Entry<String, List<Artifact>>> testEntries = testRepository.entrySet();
        for (Map.Entry<String, List<Artifact>> testEntry : testEntries) {
            list.add(addTest(testEntry.getKey(), testEntry.getValue()));
        }

        return list.toArray(new String[0]);
    }

    private String join(List<String> mavenDependencyLibraries) {
        return StringUtils.join(mavenDependencyLibraries, PATH_SEPARATOR);
    }

    private static final String DEFAULT_ENCODING = PluginTestConstants.UTF_8_NAME;

    private List<String> getVmArgs() {
        return Arrays.asList(format("file.encoding", DEFAULT_ENCODING));
    }

    private List<String> getDebugOptions() {
        return Arrays.asList("-Xdebug", "-agentlib:jdwp=transport=dt_socket,address=1296,server=y,suspend=y");
    }

    private String getAgent() {
        return String.format("-javaagent:%s=%s", context.getAgentJar(), buildAgentArguments());
    }

    private String buildAgentArguments() {
        final Map<String, String> agentArgumentMap = new LinkedHashMap<String, String>();
        agentArgumentMap.put("AGENT_TYPE", "PLUGIN_TEST");

        final List<String> importPluginIds = context.getImportPluginIds();
        if (CollectionUtils.hasLength(importPluginIds)) {
            String enablePluginIds = StringUtils.join(importPluginIds, ArtifactIdUtils.ARTIFACT_SEPARATOR);
            agentArgumentMap.put(PluginTestConstants.AGENT_PARAMETER_IMPORT_PLUGIN, enablePluginIds);
        }
        return StringUtils.join(agentArgumentMap, "=", PluginTestConstants.AGENT_PARSER_DELIMITER);
    }

    private String addTest(String testId, List<Artifact> artifactList) {
        StringBuilder mavenDependencyInfo = new StringBuilder();
        mavenDependencyInfo.append(testId);
        mavenDependencyInfo.append('=');

        for (Artifact artifact : artifactList) {
            String str = ArtifactIdUtils.artifactToString(artifact);
            if (StringUtils.hasText(str)) {
                mavenDependencyInfo.append(str);
                mavenDependencyInfo.append(ArtifactIdUtils.ARTIFACT_SEPARATOR);
            }
        }

        return mavenDependencyInfo.toString();
    }

    public String getMainClass() {
        return SharedPinpointPluginTest.class.getName();
    }

}

/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.shared.PluginSharedInstance;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.junit.platform.commons.JUnitException;
import org.tinylog.TaggedLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPluginTestSuite {
    private final TaggedLogger logger = TestLogger.getLogger();
    private static final int NO_JVM_VERSION = -1;
    public static final String DEFAULT_CONFIG_PATH = "pinpoint.config";

    private final ConfigResolver resolver = new ConfigResolver();
    private final ClassLoading classLoading = new ClassLoading();

    private final List<String> repositoryUrls;
    protected final Path testClassLocation;

    private final AgentPathAndVersion agentPathAndVersion;
    private final Path agentJar;
    private final String profile;
    private final Path configFile;
    private final Path logLocationConfig;
    private final List<String> jvmArguments;

    private final boolean debug;
    private final Class<?> testClass;
    private final List<String> importPluginIds;
    private final List<String> agentLibList;
    private final List<String> sharedLibList;
    private final List<String> junitLibList;
    private final List<String> transformIncludeList;
    private final boolean manageTraceObject;

    protected abstract List<PluginTestInstance> createTestCases(PluginTestContext context) throws Exception;

    protected abstract PluginSharedInstance createSharedInstance(PluginTestContext context) throws Exception;

    public AbstractPluginTestSuite(Class<?> testClass) {
        this.testClass = testClass;

        PinpointAgent agent = testClass.getAnnotation(PinpointAgent.class);
        this.agentPathAndVersion = resolver.getAgentPathAndVersion(agent);
        this.agentJar = resolver.resolveAgentPath(agentPathAndVersion);

        PinpointProfile profile = testClass.getAnnotation(PinpointProfile.class);
        this.profile = resolver.resolveProfile(profile);

        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);
        this.configFile = config == null ? resolveAgentConfigFileLocation(this.profile, DEFAULT_CONFIG_PATH) : resolver.resolveConfigFileLocation(config.value());

        PinpointLogLocationConfig logLocationConfig = testClass.getAnnotation(PinpointLogLocationConfig.class);
        this.logLocationConfig = logLocationConfig == null ? null : resolver.resolveConfigFileLocation(logLocationConfig.value());

        JvmArgument jvmArgument = testClass.getAnnotation(JvmArgument.class);
        this.jvmArguments = resolver.getJvmArguments(jvmArgument);

        ImportPlugin importPlugin = testClass.getAnnotation(ImportPlugin.class);
        this.importPluginIds = resolver.getImportPlugin(importPlugin);

        this.manageTraceObject = !testClass.isAnnotationPresent(TraceObjectManagable.class);

        Repository repository = testClass.getAnnotation(Repository.class);
        this.repositoryUrls = resolver.getRepository(repository);

        TransformInclude transformInclude = testClass.getAnnotation(TransformInclude.class);
        this.transformIncludeList = resolver.getTransformInclude(transformInclude);

        List<String> libs = classLoading.collectLibs(getClass().getClassLoader());

        final LibraryFilter agentLibraryFilter = new LibraryFilter(
                LibraryFilter.containsMatcher(PluginClassLoading.PLUGIN_CONTAINS_MATCHES),
                LibraryFilter.globMatcher(PluginClassLoading.PLUGIN_GLOB_MATCHES));
        this.agentLibList = classLoading.filterLibs(libs, agentLibraryFilter);

        final LibraryFilter sharedLibraryFilter = new LibraryFilter(
                LibraryFilter.containsMatcher(PluginClassLoading.TEST_MATCHES),
                LibraryFilter.containsMatcher(PluginClassLoading.PLUGIN_IT_UTILS_CONTAINS_MATCHES));
        this.sharedLibList = classLoading.filterLibs(libs, sharedLibraryFilter);

        final LibraryFilter junitLibraryFilter = new LibraryFilter(
                LibraryFilter.containsMatcher(PluginClassLoading.JUNIT_CONTAINS_MATCHES),
                LibraryFilter.containsMatcher(PluginClassLoading.PLUGIN_IT_UTILS_CONTAINS_MATCHES));
        this.junitLibList = classLoading.filterLibs(libs, junitLibraryFilter);

        this.testClassLocation = resolver.resolveTestClassLocation(testClass);

        this.debug = resolver.isDebugMode();
    }


    private Path resolveAgentConfigFileLocation(String profile, String configFile) {
        Path relativePath = this.agentPathAndVersion.getPath().resolve("profiles").resolve(profile).resolve(configFile);
        Path parent = ConfigResolver.workDir().toPath();
        while (true) {
            Path candidate = parent.resolve(relativePath);
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath();
            }
            parent = parent.getParent();

            if (parent == null) {
                throw new IllegalArgumentException("Cannot find agent path: " + relativePath);
            }
        }
    }


    public PluginSharedInstance getPluginSharedInstance() {
        try {
            PluginTestContext context = new PluginTestContext(agentJar, profile,
                    configFile, logLocationConfig, repositoryUrls,
                    testClass, testClassLocation,
                    jvmArguments, debug, importPluginIds, manageTraceObject, transformIncludeList, agentLibList, sharedLibList, junitLibList);

            return createSharedInstance(context);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw newTestError(e);
        }
    }


    public List<PluginTestInstance> getPluginTestInstanceList() {
        List<PluginTestInstance> pluginTestInstanceList = new ArrayList<>();

        try {
            PluginTestContext context = new PluginTestContext(agentJar, profile,
                    configFile, logLocationConfig, repositoryUrls,
                    testClass, testClassLocation,
                    jvmArguments, debug, importPluginIds, manageTraceObject, transformIncludeList, agentLibList, sharedLibList, junitLibList);

            pluginTestInstanceList.addAll(createTestCases(context));
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw newTestError(e);
        }

        if (pluginTestInstanceList.isEmpty()) {
            throw new JUnitException("No test");
        }

        return pluginTestInstanceList;
    }

    private RuntimeException newTestError(Exception e) {
        return new JUnitException("Fail to create test instance", e);
    }
}

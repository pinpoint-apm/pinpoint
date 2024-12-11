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

import com.navercorp.pinpoint.test.plugin.util.JavaHomeResolver;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.tinylog.TaggedLogger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPluginForkedTestSuite {
    private final TaggedLogger logger = TestLogger.getLogger();
    public static final String DEFAULT_CONFIG_PATH = "pinpoint.config";

    private static final JavaHomeResolver javaHomeResolver = JavaHomeResolver.ofSystemEnv();
    private final ConfigResolver resolver = new ConfigResolver();
    private final ClassLoading classLoading = new ClassLoading();

    private final List<String> requiredLibraries;
    private final List<String> mavenDependencyLibraries;
    private final List<String> repositoryUrls;
    private final Path testClassLocation;

    private final AgentPathAndVersion agentPathAndVersion;
    private final Path agentJar;
    private final String profile;
    private final Path configFile;
    private final Path logLocationConfig;
    private final List<String> jvmArguments;
    private final int[] jvmVersions;
    private final boolean debug;
    private final Class<?> testClass;
    private final List<String> importPluginIds;
    private final List<String> sharedLibList;

    protected abstract List<PluginForkedTestInstance> createTestCases(PluginForkedTestContext context) throws Exception;

    public AbstractPluginForkedTestSuite(Class<?> testClass) {
        this.testClass = testClass;

        PinpointAgent agent = testClass.getAnnotation(PinpointAgent.class);
        this.agentPathAndVersion = resolver.getAgentPathAndVersion(agent);
        this.agentJar = resolver.resolveAgentPath(agentPathAndVersion);

        PinpointProfile profile = testClass.getAnnotation(PinpointProfile.class);
        this.profile = resolver.resolveProfile(profile);

        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);
        this.configFile = config == null ? null : resolver.resolveConfigFileLocation(config.value());

        PinpointLogLocationConfig logLocationConfig = testClass.getAnnotation(PinpointLogLocationConfig.class);
        this.logLocationConfig = logLocationConfig == null ? null : resolver.resolveConfigFileLocation(logLocationConfig.value());

        JvmArgument jvmArgument = testClass.getAnnotation(JvmArgument.class);
        this.jvmArguments = resolver.getJvmArguments(jvmArgument);

        JvmVersion jvmVersion = testClass.getAnnotation(JvmVersion.class);
        this.jvmVersions = resolver.getJvmVersion(jvmVersion);

        ImportPlugin importPlugin = testClass.getAnnotation(ImportPlugin.class);
        this.importPluginIds = resolver.getImportPlugin(importPlugin);

        Repository repository = testClass.getAnnotation(Repository.class);
        this.repositoryUrls = resolver.getRepository(repository);

        List<String> libs = classLoading.collectLibs(getClass().getClassLoader());

        final LibraryFilter requiredLibraryFilter = new LibraryFilter(
                LibraryFilter.containsMatcher(PluginClassLoading.getContainsCheckClassPath()),
                LibraryFilter.globMatcher(PluginClassLoading.getGlobMatchesCheckClassPath()));

        this.requiredLibraries = classLoading.filterLibs(libs, requiredLibraryFilter);
        if (logger.isDebugEnabled()) {
            for (String requiredLibrary : requiredLibraries) {
                logger.debug("requiredLibraries :{}", requiredLibrary);
            }
        }

        final LibraryFilter mavenDependencyLibraryFilter = new LibraryFilter(
                LibraryFilter.containsMatcher(PluginClassLoading.MAVEN_DEPENDENCY_CLASS_PATHS));

        this.mavenDependencyLibraries = classLoading.filterLibs(libs, mavenDependencyLibraryFilter);
        if (logger.isDebugEnabled()) {
            for (String mavenDependencyLibrary : mavenDependencyLibraries) {
                logger.debug("mavenDependencyLibraries: {}", mavenDependencyLibrary);
            }
        }
        final LibraryFilter sharedLibraryFilter = new LibraryFilter(
                LibraryFilter.containsMatcher(PluginClassLoading.getContainsCheckSharedClassPath()));
        this.sharedLibList = classLoading.filterLibs(libs, sharedLibraryFilter);
        this.testClassLocation = resolver.resolveTestClassLocation(testClass);
        this.debug = resolver.isDebugMode();
    }


    protected String getJavaExecutable(int version) {
        return javaHomeResolver.buildJavaExecutable(version);
    }


    public List<PluginForkedTestInstance> getPluginTestInstanceList() {
        List<PluginForkedTestInstance> pluginTestInstanceList = new ArrayList<>();

        try {
            for (int ver : jvmVersions) {
                final String javaExe = getJavaExecutable(ver);
                if (javaExe == null) {
                    logger.error("Cannot find Java version {}. Skip test with Java {}", ver, ver);
                    continue;
                }

                PluginForkedTestContext context = new PluginForkedTestContext(agentJar, profile,
                        configFile, logLocationConfig, requiredLibraries, mavenDependencyLibraries, sharedLibList, repositoryUrls,
                        testClass, testClassLocation,
                        jvmArguments, debug, ver, javaExe, importPluginIds, null, true, null);

                List<PluginForkedTestInstance> cases = createTestCases(context);

                pluginTestInstanceList.addAll(cases);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

        return pluginTestInstanceList;
    }

}

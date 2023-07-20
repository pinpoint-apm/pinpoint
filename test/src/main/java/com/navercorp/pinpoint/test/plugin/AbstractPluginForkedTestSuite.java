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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.SystemProperty;
import com.navercorp.pinpoint.test.plugin.util.ArrayUtils;
import com.navercorp.pinpoint.test.plugin.util.CodeSourceUtils;
import com.navercorp.pinpoint.test.plugin.util.StringUtils;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import com.navercorp.pinpoint.test.plugin.util.TestPluginVersion;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPluginForkedTestSuite {
    private final TaggedLogger logger = TestLogger.getLogger();
    private static final int NO_JVM_VERSION = -1;
    private static final List<String> EMPTY_REPOSITORY_URLS = new ArrayList<>();
    public static final String DEFAULT_CONFIG_PATH = "pinpoint.config";
    private final List<String> requiredLibraries;
    private final List<String> mavenDependencyLibraries;
    private final List<String> repositoryUrls;
    private final String testClassLocation;
    private final String agentJar;
    private final String profile;
    private final String configFile;
    private final String logLocationConfig;
    private final List<String> jvmArguments;
    private final int[] jvmVersions;
    private final boolean debug;
    private final Class<?> testClass;
    private final List<String> importPluginIds;

    protected abstract List<PluginForkedTestInstance> createTestCases(PluginTestContext context) throws Exception;

    public AbstractPluginForkedTestSuite(Class<?> testClass) {
        this.testClass = testClass;

        PinpointAgent agent = testClass.getAnnotation(PinpointAgent.class);
        this.agentJar = resolveAgentPath(agent);

        PinpointProfile profile = testClass.getAnnotation(PinpointProfile.class);
        this.profile = resolveProfile(profile);

        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);
        this.configFile = config == null ? null : resolveConfigFileLocation(config.value());

        PinpointLogLocationConfig logLocationConfig = testClass.getAnnotation(PinpointLogLocationConfig.class);
        this.logLocationConfig = logLocationConfig == null ? null : resolveConfigFileLocation(logLocationConfig.value());

        JvmArgument jvmArgument = testClass.getAnnotation(JvmArgument.class);
        this.jvmArguments = getJvmArguments(jvmArgument);

        JvmVersion jvmVersion = testClass.getAnnotation(JvmVersion.class);
        this.jvmVersions = jvmVersion == null ? new int[]{NO_JVM_VERSION} : jvmVersion.value();

        ImportPlugin importPlugin = testClass.getAnnotation(ImportPlugin.class);
        this.importPluginIds = getImportPlugin(importPlugin);

        Repository repository = testClass.getAnnotation(Repository.class);
        this.repositoryUrls = getRepository(repository);

        List<String> libs = collectLibs(getClass().getClassLoader());

        final LibraryFilter requiredLibraryFilter = new LibraryFilter(
                LibraryFilter.createContainsMatcher(PluginClassLoading.getContainsCheckClassPath()),
                LibraryFilter.createGlobMatcher(PluginClassLoading.getGlobMatchesCheckClassPath()));

        this.requiredLibraries = filterLibs(libs, requiredLibraryFilter);
        if (logger.isDebugEnabled()) {
            for (String requiredLibrary : requiredLibraries) {
                logger.debug("requiredLibraries :{}", requiredLibrary);
            }
        }

        final LibraryFilter mavenDependencyLibraryFilter = new LibraryFilter(
                LibraryFilter.createContainsMatcher(PluginClassLoading.MAVEN_DEPENDENCY_CLASS_PATHS));

        this.mavenDependencyLibraries = filterLibs(libs, mavenDependencyLibraryFilter);
        if (logger.isDebugEnabled()) {
            for (String mavenDependencyLibrary : mavenDependencyLibraries) {
                logger.debug("mavenDependencyLibraries: {}", mavenDependencyLibrary);
            }
        }

        this.testClassLocation = resolveTestClassLocation(testClass);

        this.debug = isDebugMode();
    }

    private List<String> getImportPlugin(ImportPlugin importPlugin) {
        if (importPlugin == null) {
            return null;
        }
        String[] ids = importPlugin.value();
        if (ArrayUtils.isEmpty(ids)) {
            return null;
        }
        return Arrays.asList(ids);
    }

    private List<String> getRepository(Repository repository) {
        if (repository == null) {
            return EMPTY_REPOSITORY_URLS;
        }
        return Arrays.asList(repository.value());
    }

    private List<String> getJvmArguments(JvmArgument jvmArgument) {
        if (jvmArgument == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(jvmArgument.value());
    }

    protected String getJavaExecutable(int version) {
        StringBuilder builder = new StringBuilder();

        String javaHome;
        if (version == NO_JVM_VERSION) {
            javaHome = System.getProperty("java.home");
        } else {
            String envName = "JAVA_" + version + "_HOME";
            javaHome = System.getenv(envName);
        }

        if (javaHome == null) {
            return null;
        }

        builder.append(javaHome);
        builder.append(File.separatorChar);
        builder.append("bin");
        builder.append(File.separatorChar);
        builder.append("java");

        if (System.getProperty("os.name").contains("indows")) {
            builder.append(".exe");
        }

        return builder.toString();
    }

    private String resolveTestClassLocation(Class<?> testClass) {
        final URL testClassLocation = CodeSourceUtils.getCodeLocation(testClass);
        if (testClassLocation == null) {
            throw new IllegalStateException(testClass + " url not found");
        }
        return toPathString(testClassLocation);
    }

    private List<String> filterLibs(List<String> classPaths, LibraryFilter classPathFilter) {
        final Set<String> result = new LinkedHashSet<>();
        for (String classPath : classPaths) {
            if (classPathFilter.filter(classPath)) {
                result.add(classPath);
            }
        }
        return new ArrayList<>(result);
    }

    private List<String> collectLibs(ClassLoader sourceCl) {
        List<String> result = new ArrayList<>();
        final ClassLoader termCl = ClassLoader.getSystemClassLoader().getParent();
        for (ClassLoader cl : iterateClassLoaderChain(sourceCl, termCl)) {
            final List<String> libs = extractLibrariesFromClassLoader(cl);
            if (libs != null) {
                result.addAll(libs);
                if (logger.isDebugEnabled()) {
                    logger.debug("classLoader: {}", cl);
                    for (String lib : libs) {
                        logger.debug("  -> {}", lib);
                    }
                }
            }
        }
        return result;
    }

    private static Iterable<ClassLoader> iterateClassLoaderChain(ClassLoader src, ClassLoader term) {
        final List<ClassLoader> classLoaders = new ArrayList<>(8);
        ClassLoader cl = src;
        while (cl != term) {
            classLoaders.add(cl);
            if (cl == Object.class.getClassLoader()) {
                break;
            }
            cl = cl.getParent();
        }
        return classLoaders;
    }

    private static List<String> extractLibrariesFromClassLoader(ClassLoader cl) {
        if (cl instanceof URLClassLoader) {
            return extractLibrariesFromURLClassLoader((URLClassLoader) cl);
        }
        if (cl == ClassLoader.getSystemClassLoader()) {
            return extractLibrariesFromSystemClassLoader();
        }
        return null;
    }

    private static List<String> extractLibrariesFromURLClassLoader(URLClassLoader cl) {
        final URL[] urls = cl.getURLs();
        final List<String> paths = new ArrayList<>(urls.length);
        for (URL url : urls) {
            paths.add(normalizePath(toPathString(url)));
        }
        return paths;
    }

    private static List<String> extractLibrariesFromSystemClassLoader() {
        final String classPath = SystemProperty.INSTANCE.getProperty("java.class.path");
        if (StringUtils.isEmpty(classPath)) {
            return Collections.emptyList();
        }
        final List<String> paths = Arrays.asList(classPath.split(":"));
        return normalizePaths(paths);
    }

    @VisibleForTesting
    static String normalizePath(String classPath) {
        return Paths.get(classPath).toAbsolutePath().normalize().toString();
    }

    private static List<String> normalizePaths(List<String> classPaths) {
        final List<String> result = new ArrayList<>(classPaths.size());
        for (String cp : classPaths) {
            result.add(normalizePath(cp));
        }
        return result;
    }

    private static String toPathString(URL url) {
        return new File(url.getFile()).getAbsolutePath();
    }

    private String resolveAgentPath(PinpointAgent agent) {
        String path = getAgentPath(agent);
        String version = getVersion(agent);
        String relativePath = getRelativePath(path, version);

        File parent = new File(".").getAbsoluteFile();
        while (true) {
            File candidate = new File(parent, relativePath);
            if (candidate.exists()) {
                return candidate.getAbsolutePath();
            }

            parent = parent.getParentFile();

            if (parent == null) {
                throw new IllegalArgumentException("Cannot find agent path: " + relativePath);
            }
        }
    }

    private String getRelativePath(String path, String version) {
        String agentJar = String.format("pinpoint-bootstrap-%s.jar", version);
        if (path.endsWith("/")) {
            return path + agentJar;
        }
        return path + "/" + agentJar;
    }

    private String getAgentPath(PinpointAgent agent) {
        final String defaultPath = "agent/target/pinpoint-agent-" + TestPluginVersion.getVersion();
        if (agent == null) {
            return defaultPath;
        }
        if (StringUtils.hasLength(agent.value())) {
            return agent.value();
        }
        return defaultPath;
    }

    private String getVersion(PinpointAgent agent) {
        final String defaultVersion = TestPluginVersion.getVersion();
        if (agent == null) {
            return defaultVersion;
        }
        if (StringUtils.hasLength(agent.version())) {
            return agent.version();
        }
        return defaultVersion;
    }

    private String resolveConfigFileLocation(String configFile) {
        URL url = getClass().getResource(configFile.startsWith("/") ? configFile : "/" + configFile);

        if (url != null) {
            return toPathString(url);
        }

        File config = new File(configFile);
        if (config.exists()) {
            return config.getAbsolutePath();
        }

        throw new RuntimeException("Cannot find pinpoint configuration file: " + configFile);
    }

    private String resolveProfile(PinpointProfile profile) {
        if (profile == null) {
            return PinpointProfile.DEFAULT_PROFILE;
        }
        return profile.value();
    }

    private String resolveAgentConfigFileLocation(PinpointAgent agent, String profile, String configFile) {
        String relativePath = getAgentPath(agent) + "/profiles/" + profile + "/" + configFile;
        File parent = new File(".").getAbsoluteFile();
        while (true) {
            File candidate = new File(parent, relativePath);
            if (candidate.exists()) {
                try {
                    String url = candidate.toURI().toURL().toString();
                    System.out.println("url=" + url);
                } catch (MalformedURLException e) {
                }
                return candidate.getAbsolutePath();
            }

            parent = parent.getParentFile();

            if (parent == null) {
                throw new IllegalArgumentException("Cannot find agent path: " + relativePath);
            }
        }
    }


    private boolean isDebugMode() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
    }

    public List<PluginForkedTestInstance> getPluginTestInstanceList() {
        List<PluginForkedTestInstance> pluginTestInstanceList = new ArrayList<>();

        try {
            for (int ver : jvmVersions) {
                String javaExe = getJavaExecutable(ver);

                // TODO for now, java 17 is not mandatory to build pinpoint.
                // so failing to find java installation should not cause build failure.
                if (javaExe == null) {
                    logger.error("Cannot find Java version {}. Skip test with Java {}", ver, ver);
                    continue;
                }

                PluginTestContext context = new PluginTestContext(agentJar, profile,
                        configFile, logLocationConfig, requiredLibraries, mavenDependencyLibraries, repositoryUrls,
                        testClass, testClassLocation,
                        jvmArguments, debug, ver, javaExe, importPluginIds);

                List<PluginForkedTestInstance> cases = createTestCases(context);

                for (PluginForkedTestInstance testInstance : cases) {
                    pluginTestInstanceList.add(testInstance);
                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw newTestError(e);
        }

        if (pluginTestInstanceList.isEmpty()) {
            throw new RuntimeException("No test");
        }

        return pluginTestInstanceList;
    }

    private RuntimeException newTestError(Exception e) {
        return new RuntimeException("Fail to create test instance", e);
    }
}

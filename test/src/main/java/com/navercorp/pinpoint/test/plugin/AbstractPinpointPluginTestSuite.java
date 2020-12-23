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

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.util.ArrayUtils;
import com.navercorp.pinpoint.test.plugin.util.Assert;
import com.navercorp.pinpoint.test.plugin.util.CodeSourceUtils;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import com.navercorp.pinpoint.test.plugin.util.StringUtils;
import com.navercorp.pinpoint.test.plugin.util.TestPluginVersion;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPinpointPluginTestSuite extends Suite {

    private final TaggedLogger logger = TestLogger.getLogger();

    private static final int NO_JVM_VERSION = -1;

    private final List<String> requiredLibraries;
    private final List<String> mavenDependencyLibraries;
    private final String testClassLocation;

    private final String agentJar;
    private final String profile;
    private final String configFile;
    private final String[] jvmArguments;
    private final int[] jvmVersions;
    private final boolean debug;

    private final List<String> importPluginIds;

    public AbstractPinpointPluginTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        super(testClass, Collections.<Runner>emptyList());

        PinpointAgent agent = testClass.getAnnotation(PinpointAgent.class);
        this.agentJar = resolveAgentPath(agent);

        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);
        this.configFile = config == null ? null : resolveConfigFileLocation(config.value());

        PinpointProfile profile = testClass.getAnnotation(PinpointProfile.class);
        this.profile = resolveProfile(profile);

        JvmArgument jvmArgument = testClass.getAnnotation(JvmArgument.class);
        this.jvmArguments = getJvmArguments(jvmArgument);

        JvmVersion jvmVersion = testClass.getAnnotation(JvmVersion.class);
        this.jvmVersions = jvmVersion == null ? new int[]{NO_JVM_VERSION} : jvmVersion.value();

        ImportPlugin importPlugin = testClass.getAnnotation(ImportPlugin.class);
        this.importPluginIds = getImportPlugin(importPlugin);

        List<ClassLoaderLib> classLoaderLibs = collectLib(getClass().getClassLoader());
        if (logger.isDebugEnabled()) {
            for (ClassLoaderLib classLoaderLib : classLoaderLibs) {
                logger.debug("classLoader:{}", classLoaderLib.getClassLoader());
                for (URL lib : classLoaderLib.getLibs()) {
                    logger.debug("-> {}", classLoaderLib.getClassLoader(), lib);
                }
            }
        }

        this.requiredLibraries = filterLib(classLoaderLibs, new LibraryFilter(PluginClassLoading.REQUIRED_CLASS_PATHS));
        if (logger.isDebugEnabled()) {
            for (String requiredLibrary : requiredLibraries) {
                logger.debug("requiredLibraries :{}", requiredLibrary);
            }
        }
        this.mavenDependencyLibraries = filterLib(classLoaderLibs, new LibraryFilter(PluginClassLoading.MAVEN_DEPENDENCY_CLASS_PATHS));
        if (logger.isDebugEnabled()) {
            for (String mavenDependencyLibrary : mavenDependencyLibraries) {
                logger.debug("mavenDependencyLibraries :{}", mavenDependencyLibrary);
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

    private String[] getJvmArguments(JvmArgument jvmArgument) {
        if (jvmArgument == null) {
            return new String[0];
        }
        return jvmArgument.value();
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

    private static class ClassLoaderLib {
        private final ClassLoader cl;
        private final List<URL> libs;

        public ClassLoaderLib(ClassLoader cl, List<URL> libs) {
            this.cl = cl;
            this.libs = libs;
        }

        public ClassLoader getClassLoader() {
            return cl;
        }

        public List<URL> getLibs() {
            return libs;
        }
    }

    private List<String> filterLib(List<ClassLoaderLib> classLoaderLibs, LibraryFilter classPathFilter) {
        Set<String> result = new HashSet<>();
        for (ClassLoaderLib classLoaderLib : classLoaderLibs) {
            List<URL> libs = classLoaderLib.getLibs();
            for (URL lib : libs) {
                final String filterLibs = classPathFilter.filter(lib);
                if (filterLibs != null) {
                    result.add(filterLibs);
                }
            }
        }
        List<String> libs = new ArrayList<>(result);
        Collections.sort(libs);
        return libs;
    }

    private List<ClassLoaderLib> collectLib(ClassLoader cl) {
        List<ClassLoaderLib> libs = new ArrayList<>();
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                URLClassLoader ucl = ((URLClassLoader) cl);
                URL[] urLs = ucl.getURLs();
                libs.add(new ClassLoaderLib(cl, Arrays.asList(urLs)));
            }

            cl = cl.getParent();
        }
        return libs;
    }

    public static class LibraryFilter {
        private final String[] paths;

        public LibraryFilter(String[] paths) {
            this.paths = Assert.requireNonNull(paths, "paths");
        }

        public String filter(URL url) {
            if (include(url.getFile())) {
                return toPathString(url);
            }
            return null;
        }

        private boolean include(String filePath) {
            for (String required : paths) {
                if (filePath.contains(required)) {
                    return true;
                }
            }
            return false;
        }
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

    private boolean isDebugMode() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(BeforePinpointPluginTest.class);
        return befores.isEmpty() ? statement : new RunBefores(statement, befores, null);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterPinpointPluginTest.class);
        return afters.isEmpty() ? statement : new RunAfters(statement, afters, null);
    }

    @Override
    protected List<Runner> getChildren() {
        List<Runner> runners = new ArrayList<Runner>();

        try {
            for (int ver : jvmVersions) {
                String javaExe = getJavaExecutable(ver);

                // TODO for now, java 8 is not mandatory to build pinpoint.
                // so failing to find java installation should not cause build failure.
                if (javaExe == null) {
                    logger.error("Cannot find Java version {}. Skip test with Java {}", ver, ver);
                    continue;
                }

                PluginTestContext context = new PluginTestContext(agentJar, profile,
                        configFile, requiredLibraries, mavenDependencyLibraries,
                        getTestClass().getJavaClass(), testClassLocation,
                        jvmArguments, debug, ver, javaExe, importPluginIds);

                List<PinpointPluginTestInstance> cases = createTestCases(context);

                for (PinpointPluginTestInstance testInstance : cases) {
                    runners.add(new PinpointPluginTestRunner(context, testInstance));
                }
            }

        } catch (InitializationError junitError) {
            logger.error(junitError, "junit error :{}", junitError.getMessage());
            // handle MultipleFailureException ?
            List<Throwable> causes = junitError.getCauses();
            for (Throwable cause : causes) {
                logger.warn(cause, "junit error Caused By:{}", cause.getMessage());
            }
            throw newTestError(junitError);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            throw newTestError(e);
        }

        if (runners.isEmpty()) {
            throw new RuntimeException("No test");
        }

        return runners;
    }

    private RuntimeException newTestError(Exception e) {
        return new RuntimeException("Fail to create test runners", e);
    }

    protected abstract List<PinpointPluginTestInstance> createTestCases(PluginTestContext context) throws Exception;
}

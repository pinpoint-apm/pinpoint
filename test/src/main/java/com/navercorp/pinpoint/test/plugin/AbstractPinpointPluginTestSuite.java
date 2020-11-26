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

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.SystemProperty;
import com.navercorp.pinpoint.exception.PinpointException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPinpointPluginTestSuite extends Suite {
    private static final int NO_JVM_VERSION = -1;

    private static final String[] REQUIRED_CLASS_PATHS = new String[]{
            "junit", // JUnit
            "hamcrest-core", // for JUnit
            "pinpoint-test", // pinpoint-test-{VERSION}.jar
            "/test/target/classes" // pinpoint-test build output directory
    };

    private static final String[] MAVEN_DEPENDENCY_CLASS_PATHS = new String[]{
            "maven-resolver",
            "commons-lang3",
            "apache/maven",
            "guava",
            "plexus",
            "pinpoint-test",
            "slf4j-api",
            "slf4j-jdk14",
            "/test/target/classes" // pinpoint-test build output directory
    };

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
        super(testClass, Collections.<Runner> emptyList());

        PinpointAgent agent = testClass.getAnnotation(PinpointAgent.class);
        this.agentJar = resolveAgentPath(agent);

        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);
        this.configFile = config == null ? null : resolveConfigFileLocation(config.value());

        PinpointProfile profile = testClass.getAnnotation(PinpointProfile.class);
        this.profile = resolveProfile(profile);

        JvmArgument jvmArgument = testClass.getAnnotation(JvmArgument.class);
        this.jvmArguments = getJvmArguments(jvmArgument);

        JvmVersion jvmVersion = testClass.getAnnotation(JvmVersion.class);
        this.jvmVersions = jvmVersion == null ? new int[] { NO_JVM_VERSION } : jvmVersion.value();

        ImportPlugin importPlugin = testClass.getAnnotation(ImportPlugin.class);
        this.importPluginIds = getImportPlugin(importPlugin);

        this.requiredLibraries = getClassPathList(new LibraryFilter(REQUIRED_CLASS_PATHS));
        this.mavenDependencyLibraries = getClassPathList(new LibraryFilter(MAVEN_DEPENDENCY_CLASS_PATHS));
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
            javaHome = SystemProperty.INSTANCE.getProperty("java.home");
        } else {
            String envName = "JAVA_" + version + "_HOME";  
            javaHome = SystemProperty.INSTANCE.getEnv(envName);
        }
        
        if (javaHome == null) {
            return null;
        }
        
        builder.append(javaHome);
        builder.append(File.separatorChar);
        builder.append("bin");
        builder.append(File.separatorChar);
        builder.append("java");
        
        if (SystemProperty.INSTANCE.getProperty("os.name").contains("indows")) {
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


    private List<String> getClassPathList(LibraryFilter classPathFilter) {
        List<String> result = new ArrayList<String>();

        ClassLoader cl = getClass().getClassLoader();

        while (true) {
            if (cl instanceof URLClassLoader) {
                URLClassLoader ucl = ((URLClassLoader) cl);
                Collection<String> requiredLibraries = classPathFilter.filter(ucl.getURLs());
                result.addAll(requiredLibraries);
            }

            cl = cl.getParent();

            if (cl == null) {
                break;
            }
        }

        return result;
    }

    public static class LibraryFilter {
        private final String[] paths;

        public LibraryFilter(String[] paths) {
            this.paths = Assert.requireNonNull(paths, "paths");
        }

        public Collection<String> filter(URL[] urls) {
            final Set<String> result = new HashSet<>();
            for (URL url : urls) {
                if (include(url.getFile())) {
                    result.add(toPathString(url));
                }
            }
            return result;
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
        String path = agent == null ? "agent/target/pinpoint-agent-" + Version.VERSION : agent.value();
        String version = agent == null ? Version.VERSION : agent.version();
        String relativePath = path + (!path.endsWith("/") ? "/" : "") + "pinpoint-bootstrap-" + version + ".jar";

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
    
    private String resolveConfigFileLocation(String configFile) {
        URL url = getClass().getResource(configFile.startsWith("/") ? configFile : "/" + configFile);
        
        if (url != null) {
            return toPathString(url);
        }
        
        File config = new File(configFile);
        if (config.exists()) {
            return config.getAbsolutePath();
        }
        
        throw new PinpointException("Cannot find pinpoint configuration file: " + configFile);
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
        List<FrameworkMethod> befores= getTestClass().getAnnotatedMethods(BeforePinpointPluginTest.class);
        return befores.isEmpty() ? statement : new RunBefores(statement, befores, null);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        List<FrameworkMethod> afters= getTestClass().getAnnotatedMethods(AfterPinpointPluginTest.class);
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
                    System.out.println("Cannot find Java version " + ver + ". Skip test with Java " + ver);
                    continue;
                }

                PinpointPluginTestContext context = new PinpointPluginTestContext(agentJar, profile,
                        configFile, requiredLibraries, mavenDependencyLibraries,
                        getTestClass().getJavaClass(), testClassLocation,
                        jvmArguments, debug, ver, javaExe, importPluginIds);
                
                List<PinpointPluginTestInstance> cases = createTestCases(context);
                
                for (PinpointPluginTestInstance c : cases) {
                    runners.add(new PinpointPluginTestRunner(context, c));
                }
            }

        } catch (InitializationError junitError) {
            // handle MultipleFailureException ?
            List<Throwable> causes = junitError.getCauses();
            for (Throwable cause : causes) {
                System.out.println("junit error Caused By:" + cause.getMessage());
                cause.printStackTrace();
            }
            throw newTestError(junitError);
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
    
    protected abstract List<PinpointPluginTestInstance> createTestCases(PinpointPluginTestContext context) throws Exception;
}

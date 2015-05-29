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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.util.SystemProperty;
import com.navercorp.pinpoint.exception.PinpointException;

public abstract class AbstractPinpointPluginTestSuite extends Suite {
    private static final int NO_JVM_VERSION = -1;
    
    private static final String[] REQUIRED_CLASS_PATHS = new String[] {
        "junit", // JUnit
        "hamcrest-core", // for JUnit
        "pinpoint-test", // pinpoint-test-{VERSION}.jar
        "/test/target/classes" // pinpoint-test build output directory
    };

    private final List<String> requiredLibraries;
    private final String testClassLocation;

    private final String agentJar;
    private final String configFile;
    private final String[] jvmArguments;
    private final int[] jvmVersions;
    private final boolean debug;

    public AbstractPinpointPluginTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        super(testClass, Collections.<Runner> emptyList());

        PinpointAgent agent = testClass.getAnnotation(PinpointAgent.class);
        this.agentJar = resolveAgentPath(agent);

        PinpointConfig config = testClass.getAnnotation(PinpointConfig.class);
        this.configFile = config == null ? null : resolveConfigFileLocation(config.value());

        JvmArgument jvmArgument = testClass.getAnnotation(JvmArgument.class);
        this.jvmArguments = jvmArgument == null ? new String[0] : jvmArgument.value();

        JvmVersion jvmVersion = testClass.getAnnotation(JvmVersion.class);
        this.jvmVersions = jvmVersion == null ? new int[] { NO_JVM_VERSION } : jvmVersion.value();

        this.requiredLibraries = resolveRequiredLibraries();
        this.testClassLocation = resolveTestClassLocation(testClass);
        this.debug = isDebugMode();
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
        CodeSource codeSource = testClass.getProtectionDomain().getCodeSource();
        URL testClassLocation = codeSource.getLocation();

        return toPathString(testClassLocation);
    }

    private List<String> resolveRequiredLibraries() {
        List<String> result = new ArrayList<String>();
        
        ClassLoader cl = getClass().getClassLoader();
        
        while (true) {
            if (cl instanceof URLClassLoader) {
                findRequiredLibraries(result, (URLClassLoader)cl);
            }
            
            cl = cl.getParent();
            
            if (cl == null) {
                break;
            }
        }

        return result;
    }


    private void findRequiredLibraries(List<String> result, URLClassLoader cl) {
        outer: for (URL url : cl.getURLs()) {
            for (String required : REQUIRED_CLASS_PATHS) {
                if (url.getFile().contains(required)) {
                    result.add(toPathString(url));

                    continue outer;
                }
            }
        }
    }

    private String toPathString(URL url) {
        try {
            return new File(url.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException("???", e);
        }
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
                
                PinpointPluginTestContext context = new PinpointPluginTestContext(agentJar, configFile, requiredLibraries, getTestClass().getJavaClass(), testClassLocation, jvmArguments, debug, ver, javaExe); 
                
                List<PinpointPluginTestInstance> cases = createTestCases(context);
                
                for (PinpointPluginTestInstance c : cases) {
                    runners.add(new PinpointPluginTestRunner(context, c));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Fail to create test runners", e);
        }
        
        if (runners.isEmpty()) {
            throw new RuntimeException("No test");
        }
        
        return runners;
    }
    
    protected abstract List<PinpointPluginTestInstance> createTestCases(PinpointPluginTestContext context) throws Exception;
}

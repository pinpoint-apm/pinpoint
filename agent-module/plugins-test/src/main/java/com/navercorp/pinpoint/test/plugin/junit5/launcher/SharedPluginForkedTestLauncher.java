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

package com.navercorp.pinpoint.test.plugin.junit5.launcher;

import com.navercorp.pinpoint.test.plugin.PluginClassLoading;
import com.navercorp.pinpoint.test.plugin.ReflectPluginTestVerifier;
import com.navercorp.pinpoint.test.plugin.TraceObjectManagable;
import com.navercorp.pinpoint.test.plugin.shared.ReflectionDependencyResolver;
import com.navercorp.pinpoint.test.plugin.shared.SharedPluginTestConstants;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllInvoker;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestExecutor;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleWrapper;
import com.navercorp.pinpoint.test.plugin.shared.TestInfo;
import com.navercorp.pinpoint.test.plugin.shared.TestParameter;
import com.navercorp.pinpoint.test.plugin.shared.TestParameterParser;
import com.navercorp.pinpoint.test.plugin.shared.ThreadFactory;
import com.navercorp.pinpoint.test.plugin.util.ArrayUtils;
import com.navercorp.pinpoint.test.plugin.util.ChildFirstClassLoader;
import com.navercorp.pinpoint.test.plugin.util.CollectionUtils;
import com.navercorp.pinpoint.test.plugin.util.ProfilerClass;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import com.navercorp.pinpoint.test.plugin.util.ThreadContextCallable;
import com.navercorp.pinpoint.test.plugin.util.URLUtils;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class SharedPluginForkedTestLauncher {

    private static final TaggedLogger logger = TestLogger.getLogger();

    public static void main(String[] args) throws Exception {
        final String mavenDependencyResolverClassPaths = System.getProperty(SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS);
        if (mavenDependencyResolverClassPaths == null) {
            logger.error("mavenDependencyResolverClassPaths must not be empty");
            return;
        }

        final String repositoryUrlString = System.getProperty(SharedPluginTestConstants.TEST_REPOSITORY_URLS);
        if (repositoryUrlString == null) {
            logger.error("repositoryUrls must not be empty");
            return;
        }
        logger.debug("-D{}={}", SharedPluginTestConstants.TEST_REPOSITORY_URLS, repositoryUrlString);

        final String testLocation = System.getProperty(SharedPluginTestConstants.TEST_LOCATION);
        if (testLocation == null) {
            logger.error("testLocation must not be empty");
            return;
        }
        logger.debug("-D{}={}", SharedPluginTestConstants.TEST_LOCATION, testLocation);

        final String testClazzName = System.getProperty(SharedPluginTestConstants.TEST_CLAZZ_NAME);
        if (testClazzName == null) {
            logger.error("testClazzName must not be empty");
            return;
        }
        logger.debug("-D{}={}", SharedPluginTestConstants.TEST_CLAZZ_NAME, testClazzName);

        String loggerEnable = System.getProperty(SharedPluginTestConstants.TEST_LOGGER);
        if (loggerEnable == null) {
            logger.debug("-D{} is not set", SharedPluginTestConstants.TEST_LOGGER);
            loggerEnable = Boolean.TRUE.toString();
        }
        final boolean testLogger = Boolean.parseBoolean(loggerEnable);
        logger.debug("-D{}={}", SharedPluginTestConstants.TEST_LOGGER, testLogger);

        if (ArrayUtils.isEmpty(args)) {
            logger.error("test must not be empty");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("main args:{}", Arrays.toString(args));
        }

        String[] mavenDependencyResolverClassPathArray = mavenDependencyResolverClassPaths.split(File.pathSeparator);
        String[] repositoryUrls = repositoryUrlString.split(",");
        TestParameterParser parser = new TestParameterParser();
        List<TestParameter> testParameters = parser.parse(args);
        SharedPluginForkedTestLauncher pluginTest = new SharedPluginForkedTestLauncher(testClazzName, testLocation, testLogger,
                mavenDependencyResolverClassPathArray, repositoryUrls, testParameters, System.out);
        pluginTest.execute();

    }

    private final String testClazzName;
    private final String testLocation;
    private final boolean testLogger;
    private final String[] mavenDependencyResolverClassPaths;
    private final String[] repositoryUrls;
    private final List<TestParameter> testParameters;
    private final PrintStream out;

    public SharedPluginForkedTestLauncher(String testClazzName, String testLocation, boolean testLogger,
                                          String[] mavenDependencyResolverClassPaths, String[] repositoryUrls,
                                          List<TestParameter> testParameters, PrintStream out) {
        this.testClazzName = testClazzName;
        this.testLocation = testLocation;
        this.testLogger = testLogger;
        this.mavenDependencyResolverClassPaths = mavenDependencyResolverClassPaths;
        this.repositoryUrls = repositoryUrls;
        this.testParameters = testParameters;
        this.out = out;
    }

    private List<TestInfo> newTestCaseInfo(List<TestParameter> testParameters, File testClazzLocation, String[] repositoryUrls, ClassLoader dependencyClassLoader) throws Exception {
        ReflectionDependencyResolver dependencyResolver = new ReflectionDependencyResolver(dependencyClassLoader, repositoryUrls);
        List<File> loggerDependencies = getLoggerDependencies(dependencyResolver, dependencyClassLoader);
        logger.debug("loggerDependency:{}", loggerDependencies);

        List<TestInfo> testInfos = new ArrayList<>();
        for (TestParameter testParameter : testParameters) {
            final List<File> testDependency = new ArrayList<>();
            testDependency.add(testClazzLocation);

            testDependency.addAll(loggerDependencies);

            List<File> testParameterDependency = getTestParameterDependency(dependencyClassLoader, dependencyResolver, testParameter);
            testDependency.addAll(testParameterDependency);

            final TestInfo testInfo = new TestInfo(testParameter.getTestId(), testDependency, Arrays.asList(repositoryUrls));
            testInfos.add(testInfo);
        }
        return testInfos;
    }

    private List<File> getTestParameterDependency(ClassLoader mavenDependencyResolverClassLoader,
                                                  ReflectionDependencyResolver dependencyResolver,
                                                  TestParameter testParameter) throws Exception {

        final List<String> mavenDependencies = testParameter.getMavenDependencies();
        List<File> testDependencyFileList = lookup(dependencyResolver, mavenDependencies, mavenDependencyResolverClassLoader);
        if (logger.isDebugEnabled()) {
            logger.debug("@Dependency {}", mavenDependencies);
            for (File file : testDependencyFileList) {
                logger.debug("-> {}", file);
            }
        }
        return testDependencyFileList;
    }

    private List<File> getLoggerDependencies(ReflectionDependencyResolver dependencyResolver, ClassLoader mavenDependencyResolverClassLoader) throws Exception {
        if (!testLogger) {
            return Collections.emptyList();
        }
        List<String> dependencyLib = PluginClassLoading.LOGGER_DEPENDENCY;
        List<File> libFiles = lookup(dependencyResolver, dependencyLib, mavenDependencyResolverClassLoader);
        if (logger.isDebugEnabled()) {
            logger.debug("LoggerDependency {}", dependencyLib);
            for (File libFile : libFiles) {
                logger.debug("-> {}", libFile);
            }
        }
        return libFiles;
    }

    private List<File> lookup(final ReflectionDependencyResolver dependencyResolver, final List<String> dependencyLib, ClassLoader cl) throws Exception {
        Callable<List<File>> callable = new ThreadContextCallable<>(new Callable<List<File>>() {
            @Override
            public List<File> call() throws Exception {
                return dependencyResolver.lookup(dependencyLib);
            }
        }, cl);
        return callable.call();
    }

    private void logTestInformation() {
        logger.info("[{}] {}", getClass().getSimpleName(), testClazzName);

        if (logger.isDebugEnabled()) {
            for (String mavenDependencyResolverClassPath : mavenDependencyResolverClassPaths) {
                logger.debug("{}: {}", SharedPluginTestConstants.MAVEN_DEPENDENCY_RESOLVER_CLASS_PATHS, mavenDependencyResolverClassPath);
            }
            for (TestParameter testParameter : testParameters) {
                logger.debug("{} {}", testClazzName, testParameter);
            }
            for (String repositoryUrl : repositoryUrls) {
                logger.debug("{}: {}", SharedPluginTestConstants.TEST_REPOSITORY_URLS, repositoryUrl);
            }
        }
    }

    public void execute() throws Exception {
        logTestInformation();
        ClassLoader mavenDependencyResolverClassLoader = new ChildFirstClassLoader(URLUtils.fileToUrls(mavenDependencyResolverClassPaths));
        File testClazzLocation = new File(testLocation);
        List<TestInfo> testInfos = newTestCaseInfo(testParameters, testClazzLocation, repositoryUrls, mavenDependencyResolverClassLoader);

        executes(testInfos);
    }

    private void executes(List<TestInfo> testInfos) {
        if (!CollectionUtils.hasLength(testInfos)) {
            return;
        }

        TestInfo firstTestInfo = testInfos.get(0);
        final ClassLoader sharedClassLoader = createTestClassLoader(firstTestInfo);
        SharedTestExecutor sharedTestExecutor = new SharedTestExecutor(testClazzName, sharedClassLoader);

        sharedTestExecutor.startBefore(10, TimeUnit.MINUTES);


        final SharedTestLifeCycleWrapper sharedTestLifeCycleWrapper = sharedTestExecutor.getSharedClassWrapper();
        for (TestInfo testInfo : testInfos) {
            execute(testInfo, sharedTestLifeCycleWrapper);
        }

        sharedTestExecutor.startAfter(5, TimeUnit.MINUTES);
    }

    private ClassLoader createTestClassLoader(TestInfo testInfo) {
        List<File> dependencyFileList = testInfo.getDependencyFileList();
        if (logger.isDebugEnabled()) {
            for (File dependency : dependencyFileList) {
                logger.debug("testcase cl lib :{}", dependency);
            }
        }
        URL[] urls = URLUtils.fileToUrls(dependencyFileList);
        return new ChildFirstClassLoader(urls, ProfilerClass.PINPOINT_PROFILER_CLASS);
    }

    private void execute(final TestInfo testInfo, SharedTestLifeCycleWrapper sharedTestLifeCycleWrapper) {
        try {
            final ClassLoader testClassLoader = createTestClassLoader(testInfo);
            final SharedPluginForkedTestLauncherListener listener = new SharedPluginForkedTestLauncherListener(testInfo.getTestId());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    final Class<?> testClazz = loadClass();
                    boolean manageTraceObject = !testClazz.isAnnotationPresent(TraceObjectManagable.class);

                    SharedTestBeforeAllInvoker invoker = new SharedTestBeforeAllInvoker(testClazz);
                    try {
                        if(sharedTestLifeCycleWrapper != null) {
                            invoker.invoke(sharedTestLifeCycleWrapper.getLifeCycleResult());
                        }
                    } catch (Throwable th) {
                        logger.error(th, "invoker setter method failed. testClazz:{} testId:{}", testClazzName, testInfo.getTestId());
                    }

                    try {
                        listener.executionStarted();
                        LauncherConfig launcherConfig = LauncherConfig.builder()
                                .enableTestEngineAutoRegistration(false)
                                .enableLauncherSessionListenerAutoRegistration(false)
                                .enableLauncherDiscoveryListenerAutoRegistration(false)
                                .enablePostDiscoveryFilterAutoRegistration(false)
                                .enableTestExecutionListenerAutoRegistration(false)
                                .addTestEngines(new JupiterTestEngine())
                                .addTestExecutionListeners(new SharedPluginForkedTestExecutionListener(testInfo.getTestId()))
                                .addTestExecutionListeners(new SharedPluginForkedTestVerifierExecutionListener(manageTraceObject))
                                .build();

                        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                                .selectors(DiscoverySelectors.selectClass(testClazz))
                                .build();
                        LauncherSession session = LauncherFactory.openSession(launcherConfig);
                        session.getLauncher().execute(request);
                        listener.executionFinished(TestExecutionResult.successful());
                    } catch (Throwable t) {
                        t.printStackTrace();
                        listener.executionFinished(TestExecutionResult.failed(t));
                    }
                }

                private Class<?> loadClass() {
                    try {
                        return testClassLoader.loadClass(testClazzName);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            String threadName = testClazzName + " " + testInfo.getTestId() + " Thread";

            ThreadFactory threadFactory = new ThreadFactory(threadName, testClassLoader);
            Thread testThread = threadFactory.newThread(runnable);
            testThread.start();

            testThread.join(TimeUnit.MINUTES.toMillis(5));
            checkTerminatedState(testThread, testClazzName + " " + testInfo.getTestId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReflectPluginTestVerifier.getInstance().cleanUp(true);
        }
    }


    private void checkTerminatedState(Thread testThread, String testInfo) {
        if (testThread.isAlive()) {
            throw new IllegalStateException(testInfo + " not finished");
        }
    }
}

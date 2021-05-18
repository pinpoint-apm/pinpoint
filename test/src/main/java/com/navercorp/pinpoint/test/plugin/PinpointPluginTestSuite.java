/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.shared.SharedProcessManager;
import com.navercorp.pinpoint.test.plugin.shared.SharedProcessPluginTestCase;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.navercorp.pinpoint.test.plugin.PluginTestConstants.CHILD_CLASS_PATH_PREFIX;

/**
 * We have referred OrderedThreadPoolExecutor ParentRunner of JUnit.
 *
 * @author Jongho Moon
 * @author Taejin Koo
 */
public class PinpointPluginTestSuite extends AbstractPinpointPluginTestSuite {
    private static final String DEFAULT_ENCODING = PluginTestConstants.UTF_8_NAME;

    private final TaggedLogger logger = TestLogger.getLogger();

    private final boolean testOnSystemClassLoader;
    private final boolean testOnChildClassLoader;
    private final String[] repositories;

    private static final DependencyResolverFactory RESOLVER_FACTORY = new DependencyResolverFactory();

    private final String[] dependencies;
    private final String libraryPath;
    private final String[] librarySubDirs;

    private final boolean sharedProcess;

    private final Object childrenLock = new Object();
    private volatile Collection<Runner> filteredChildren = null;

    private final RunnerScheduler scheduler = new RunnerScheduler() {
        public void schedule(Runnable childStatement) {
            childStatement.run();
        }

        public void finished() {
            // do nothing
        }
    };

    protected boolean usingSharedProcess() {
        return true;
    }

    public PinpointPluginTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        this(testClass, true);
    }

    public PinpointPluginTestSuite(Class<?> testClass, boolean sharedProcess) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        super(testClass);

        OnClassLoader onClassLoader = testClass.getAnnotation(OnClassLoader.class);
        this.testOnChildClassLoader = onClassLoader == null ? true : onClassLoader.child();
        this.testOnSystemClassLoader = onClassLoader == null ? false : onClassLoader.system();

        Dependency deps = testClass.getAnnotation(Dependency.class);
        this.dependencies = deps == null ? null : deps.value();

        TestRoot lib = testClass.getAnnotation(TestRoot.class);

        if (lib == null) {
            this.libraryPath = null;
            this.librarySubDirs = null;
        } else {
            String path = lib.value();

            if (path.isEmpty()) {
                path = lib.path();
            }

            this.libraryPath = path;
            this.librarySubDirs = lib.libraryDir();
        }

        if (deps != null && lib != null) {
            throw new IllegalArgumentException("@Dependency and @TestRoot can not annotate a class at the same time");
        }

        Repository repos = testClass.getAnnotation(Repository.class);
        this.repositories = repos == null ? new String[0] : repos.value();
        this.sharedProcess = sharedProcess;
    }

    @Override
    protected List<PinpointPluginTestInstance> createTestCases(PluginTestContext context) throws Exception {
        if (dependencies != null) {
            if (sharedProcess) {
                return createSharedCasesWithDependencies(context);
            } else {
                return createCasesWithDependencies(context);
            }
        } else if (libraryPath != null) {
            return createCasesWithLibraryPath(context);
        }

        return createCasesWithJdkOnly(context);
    }

    private List<PinpointPluginTestInstance> createSharedCasesWithDependencies(PluginTestContext context) throws ArtifactResolutionException, DependencyResolutionException {
        List<PinpointPluginTestInstance> cases = new ArrayList<>();

        DependencyResolver resolver = getDependencyResolver(this.repositories);

        Map<String, List<Artifact>> dependencyMap = resolver.resolveDependencySets(dependencies);
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, List<Artifact>> entry : dependencyMap.entrySet()) {
                logger.debug("{} {}", entry.getKey(), entry.getValue());
            }
        }

        SharedProcessManager sharedProcessManager = new SharedProcessManager(context);
        for (Map.Entry<String, List<Artifact>> artifactEntry : dependencyMap.entrySet()) {
            final String testId = artifactEntry.getKey();
            final List<Artifact> artifacts = artifactEntry.getValue();

            List<String> libs = getAbsolutePath(resolver, artifacts);

            PinpointPluginTestInstance testInstance = newSharedProcessPluginTestCase(context, testId, libs, sharedProcessManager);

            cases.add(testInstance);
            sharedProcessManager.registerTest(testInstance.getTestId(), artifacts);
        }

        return cases;
    }

    private List<String> getAbsolutePath(DependencyResolver resolver, List<Artifact> artifacts) throws DependencyResolutionException {
        List<String> libs = new ArrayList<>();
        for (File lib : resolver.resolveArtifactsAndDependencies(artifacts)) {
            libs.add(lib.getAbsolutePath());
        }
        return libs;
    }

    private PinpointPluginTestInstance newSharedProcessPluginTestCase(PluginTestContext context, String testId, List<String> libs, SharedProcessManager sharedProcessManager) {
        if (testOnSystemClassLoader) {
            return new SharedProcessPluginTestCase(context, testId, libs, true, sharedProcessManager);
        }
        if (testOnChildClassLoader) {
            return new SharedProcessPluginTestCase(context, testId, libs, false, sharedProcessManager);
        }
        throw new IllegalStateException("Illegal classLoader");
    }

    private DependencyResolver getDependencyResolver(String[] repositories) {
        return RESOLVER_FACTORY.get(repositories);
    }

    private List<PinpointPluginTestInstance> createCasesWithDependencies(PluginTestContext context) throws ArtifactResolutionException, DependencyResolutionException {
        List<PinpointPluginTestInstance> cases = new ArrayList<>();

        DependencyResolver resolver = getDependencyResolver(repositories);
        Map<String, List<Artifact>> dependencyCases = resolver.resolveDependencySets(dependencies);

        for (Map.Entry<String, List<Artifact>> dependencyCase : dependencyCases.entrySet()) {
            List<String> libs = getAbsolutePath(resolver, dependencyCase.getValue());

            String testId = dependencyCase.getKey();
            PinpointPluginTestInstance testCase = newNormalPluginTestCase(context, testId, libs);
            cases.add(testCase);
        }

        return cases;
    }

    private PinpointPluginTestInstance newNormalPluginTestCase(PluginTestContext context, String testId, List<String> libs) {
        if (testOnSystemClassLoader) {
            return new NormalPluginTestCase(context, testId, libs, true);
        }
        if (testOnChildClassLoader) {
            return new NormalPluginTestCase(context, testId, libs, false);
        }
        throw new IllegalStateException("Illegal classLoader");
    }

    private List<PinpointPluginTestInstance> createCasesWithLibraryPath(PluginTestContext context) {
        File file = new File(libraryPath);

        if (!file.isDirectory()) {
            throw new RuntimeException("value of @TestRoot is not a directory: " + libraryPath);
        }
        File[] children = file.listFiles();
        if (children == null) {
            return Collections.emptyList();
        }

        List<PinpointPluginTestInstance> cases = new ArrayList<>();
        for (File child : children) {
            if (!child.isDirectory()) {
                continue;
            }

            List<String> libraries = new ArrayList<>();

            if (librarySubDirs.length == 0) {
                addJars(child, libraries);
                libraries.add(child.getAbsolutePath());
            } else {
                for (String subDir : librarySubDirs) {
                    File libDir = new File(child, subDir);
                    addJars(libDir, libraries);
                    libraries.add(libDir.getAbsolutePath());
                }
            }

            PinpointPluginTestInstance testCase = newNormalPluginTestCase(context, child.getName(), libraries);
            cases.add(testCase);
        }

        return cases;
    }

    private List<PinpointPluginTestInstance> createCasesWithJdkOnly(PluginTestContext context) {
        PinpointPluginTestInstance testCase = newNormalPluginTestCase(context, "", Collections.<String>emptyList());

        List<PinpointPluginTestInstance> cases = new ArrayList<>();
        cases.add(testCase);
        return cases;
    }

    private void addJars(File libDir, List<String> libraries) {
        if (!libDir.isDirectory()) {
            return;
        }
        File[] children = libDir.listFiles();
        if (children == null) {
            return;
        }
        for (File f : children) {
            if (f.getName().endsWith(".jar")) {
                libraries.add(f.getAbsolutePath());
            }
        }
    }

    protected Statement classBlock(final RunNotifier notifier) {
        Statement statement = childrenInvoker(notifier);

        if (!areAllChildrenIgnored()) {
            statement = withBeforeClasses(statement);
            statement = withAfterClasses(statement);
            statement = withClassRules(statement);
        }

        return statement;
    }

    private Statement withClassRules(Statement statement) {
        List<TestRule> classRules = classRules();
        return classRules.isEmpty() ? statement :
                new RunRules(statement, classRules, getDescription());
    }

    private boolean areAllChildrenIgnored() {
        for (Runner child : getFilteredChildren()) {
            if (!isIgnored(child)) {
                return false;
            }
        }
        return true;
    }

    private Collection<Runner> getFilteredChildren() {
        if (filteredChildren == null) {
            synchronized (childrenLock) {
                if (filteredChildren == null) {
                    filteredChildren = Collections.unmodifiableCollection(getChildren());
                }
            }
        }
        return filteredChildren;
    }

    public void sort(Sorter sorter) {
        synchronized (childrenLock) {
            for (Runner each : getFilteredChildren()) {
                sorter.apply(each);
            }
            List<Runner> sortedChildren = new ArrayList<>(getFilteredChildren());
            Collections.sort(sortedChildren, comparator(sorter));
            filteredChildren = Collections.unmodifiableCollection(sortedChildren);
        }
    }

    private Comparator<? super Runner> comparator(final Sorter sorter) {
        return new Comparator<Runner>() {
            public int compare(Runner o1, Runner o2) {
                return sorter.compare(describeChild(o1), describeChild(o2));
            }
        };
    }

    private void runChildren(final RunNotifier notifier) {
        final RunnerScheduler currentScheduler = scheduler;

        try {
            for (final Runner each : getFilteredChildren()) {
                currentScheduler.schedule(new Runnable() {
                    public void run() {
                        runChild(each, notifier);
                    }
                });
            }
        } finally {
            currentScheduler.finished();
        }
    }

    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() {
                runChildren(notifier);
            }
        };
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        synchronized (childrenLock) {
            List<Runner> children = new ArrayList<>(getFilteredChildren());
            for (Iterator<Runner> iter = children.iterator(); iter.hasNext(); ) {
                Runner each = iter.next();

                if (shouldRun(filter, each)) {
                    try {
                        filter.apply(each);
                    } catch (NoTestsRemainException e) {
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
            filteredChildren = Collections.unmodifiableCollection(children);
            if (filteredChildren.isEmpty()) {
                throw new NoTestsRemainException();
            }
        }

    }

    private boolean shouldRun(Filter filter, Runner each) {
        if (filter.shouldRun(describeChild(each))) {
            return true;
        }

        if (each instanceof PinpointPluginTestRunner) {
            return ((PinpointPluginTestRunner) each).isAvailable(filter);
        }

        return false;
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(getName(), getRunnerAnnotations());
        for (Runner child : getFilteredChildren()) {
            description.addChild(describeChild(child));
        }
        return description;
    }

    private static class NormalPluginTestCase implements DelegateSupportedPinpointPluginTestInstance {
        private final PluginTestContext context;
        private final String testId;
        private final List<String> libs;
        private final boolean onSystemClassLoader;
        private final ProcessManager processManager;

        public NormalPluginTestCase(PluginTestContext context, String testId, List<String> libs, boolean onSystemClassLoader) {
            this.context = context;
            this.testId = testId + ":" + (onSystemClassLoader ? "system" : "child") + ":" + context.getJvmVersion();
            this.libs = libs;
            this.onSystemClassLoader = onSystemClassLoader;
            this.processManager = new DefaultProcessManager(context);
        }

        @Override
        public String getTestId() {
            return testId;
        }

        @Override
        public List<String> getClassPath() {
            if (onSystemClassLoader) {
                List<String> libs = new ArrayList<>(context.getRequiredLibraries());
                libs.addAll(this.libs);
                libs.add(context.getTestClassLocation());

                return libs;
            } else {
                return context.getRequiredLibraries();
            }
        }

        @Override
        public List<String> getVmArgs() {
            return Arrays.asList("-Dfile.encoding=" + DEFAULT_ENCODING);
        }

        @Override
        public String getMainClass() {
            return ForkedPinpointPluginTest.class.getName();
        }

        @Override
        public List<String> getAppArgs() {
            List<String> args = new ArrayList<>();

            args.add(context.getTestClass().getName());

            if (!onSystemClassLoader) {
                StringBuilder classPath = new StringBuilder();
                classPath.append(CHILD_CLASS_PATH_PREFIX);

                for (String lib : libs) {
                    classPath.append(lib);
                    classPath.append(File.pathSeparatorChar);
                }

                classPath.append(context.getTestClassLocation());
                args.add(classPath.toString());
            }

            return args;
        }

        @Override
        public Scanner startTest() throws Exception {
            Process process = processManager.create(this);
            InputStream inputStream = process.getInputStream();
            return new Scanner(inputStream, DEFAULT_ENCODING);
        }

        @Override
        public Scanner startTest(PinpointPluginTestInstance pinpointPluginTestInstance) throws Throwable {
            Process process = processManager.create(pinpointPluginTestInstance);
            InputStream inputStream = process.getInputStream();
            return new Scanner(inputStream, DEFAULT_ENCODING);
        }

        @Override
        public void endTest() throws Exception {
            processManager.stop();

            // do nothing
        }

        @Override
        public File getWorkingDirectory() {
            return new File(".");
        }

        @Override
        public String toString() {
            return "NormalPluginTestCase{" +
                    "testId='" + testId + '\'' +
                    '}';
        }
    }

}

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

import com.navercorp.pinpoint.test.plugin.shared.SharedProcessManager;
import com.navercorp.pinpoint.test.plugin.util.FileUtils;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * We have referred OrderedThreadPoolExecutor ParentRunner of JUnit.
 *
 * @author Jongho Moon
 * @author Taejin Koo
 */
public class DefaultPluginForkedTestSuite extends AbstractPluginForkedTestSuite {
    private static final String DEFAULT_ENCODING = PluginTestConstants.UTF_8_NAME;
    private static final Map<String, Object> RESOLVER_OPTION = createResolverOption();
    private static final DependencyResolverFactory RESOLVER_FACTORY = new DependencyResolverFactory(RESOLVER_OPTION);
    private final TaggedLogger logger = TestLogger.getLogger();
    private final boolean testOnSystemClassLoader;
    private final boolean testOnChildClassLoader;
    private final String[] repositories;
    private final String[] dependencies;

    private final String libraryPath;
    private final String[] librarySubDirs;

    private final boolean sharedProcess;

    private final Object childrenLock = new Object();

    private static Map<String, Object> createResolverOption() {
        Map<String, Object> resolverOption = new HashMap<>();
        resolverOption.put(ConfigurationProperties.CONNECT_TIMEOUT, TimeUnit.SECONDS.toMillis(5));
        resolverOption.put(ConfigurationProperties.REQUEST_TIMEOUT, TimeUnit.MINUTES.toMillis(5));
        return resolverOption;
    }

    public DefaultPluginForkedTestSuite(Class<?> testClass) {
        this(testClass, true);
    }

    public DefaultPluginForkedTestSuite(Class<?> testClass, boolean sharedProcess) {
        super(testClass);

        OnClassLoader onClassLoader = testClass.getAnnotation(OnClassLoader.class);
        if (onClassLoader == null) {
            this.testOnChildClassLoader = true;
        } else {
            this.testOnChildClassLoader = onClassLoader.child();
        }
        if (onClassLoader == null) {
            this.testOnSystemClassLoader = false;
        } else {
            this.testOnSystemClassLoader = onClassLoader.system();
        }

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
    protected List<PluginForkedTestInstance> createTestCases(PluginTestContext context) throws Exception {
        if (dependencies != null) {
            return createSharedCasesWithDependencies(context);
        }
        return createCasesWithJdkOnly(context);
    }

    private List<PluginForkedTestInstance> createSharedCasesWithDependencies(PluginTestContext context) throws ArtifactResolutionException, DependencyResolutionException {
        DependencyResolver resolver = getDependencyResolver(this.repositories);

        Map<String, List<Artifact>> dependencyMap = resolver.resolveDependencySets(dependencies);
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, List<Artifact>> entry : dependencyMap.entrySet()) {
                logger.debug("{} {}", entry.getKey(), entry.getValue());
            }
        }

        List<PluginForkedTestInstance> cases = new ArrayList<>();
        SharedProcessManager sharedProcessManager = new SharedProcessManager(context);
        for (Map.Entry<String, List<Artifact>> artifactEntry : dependencyMap.entrySet()) {
            final String testId = artifactEntry.getKey();
            final List<Artifact> artifacts = artifactEntry.getValue();

            List<String> libs = null;
            try {
                libs = resolveArtifactsAndDependencies(resolver, artifacts);
            } catch (DependencyResolutionException e) {
                // TODO Skip when running the test
                logger.warn(e, "resolveArtifactsAndDependencies failed testId={}", testId);
                continue;
            }

            PluginForkedTestInstance testInstance = newSharedProcessPluginTestCase(context, testId, libs, sharedProcessManager);
            cases.add(testInstance);
            sharedProcessManager.registerTest(testInstance.getTestId(), artifacts);
        }

        return cases;
    }


    private List<PluginTestInstance> createCasesWithDependencies(PluginTestContext context) throws ClassNotFoundException {
        final PluginTestInstanceFactory pluginTestInstanceFactory = new PluginTestInstanceFactory(context);
        final List<PluginTestInstance> pluginTestInstanceList = new ArrayList<>();
        final DependencyResolver resolver = getDependencyResolver(repositories);
        final Map<String, List<Artifact>> dependencyCases = resolver.resolveDependencySets(dependencies);
        for (Map.Entry<String, List<Artifact>> dependencyCase : dependencyCases.entrySet()) {
            final String testId = dependencyCase.getKey();
            final List<String> libs = new ArrayList<>();
            try {
                final List<Artifact> artifactList = dependencyCase.getValue();
                libs.addAll(resolveArtifactsAndDependencies(resolver, artifactList));
            } catch (DependencyResolutionException e) {
                logger.info("Failed to resolve artifacts and dependencies. dependency={}", dependencyCase);
            }

            final PluginTestInstance pluginTestInstance = pluginTestInstanceFactory.create(testId, libs, isOnSystemClassLoader());
            pluginTestInstanceList.add(pluginTestInstance);
        }

        return pluginTestInstanceList;
    }

    private List<String> resolveArtifactsAndDependencies(DependencyResolver resolver, List<Artifact> artifacts) throws DependencyResolutionException {
        final List<File> files = resolver.resolveArtifactsAndDependencies(artifacts);
        return FileUtils.toAbsolutePath(files);
    }

    private DependencyResolver getDependencyResolver(String[] repositories) {
        return RESOLVER_FACTORY.get(repositories);
    }

    private PluginForkedTestInstance newSharedProcessPluginTestCase(PluginTestContext context, String testId, List<String> libs, SharedProcessManager sharedProcessManager) {
        if (testOnSystemClassLoader) {
            return new SharedPluginForkedTestInstance(context, testId, libs, true, sharedProcessManager);
        }
        if (testOnChildClassLoader) {
            return new SharedPluginForkedTestInstance(context, testId, libs, false, sharedProcessManager);
        }
        throw new IllegalStateException("Illegal classLoader");
    }

    private List<PluginForkedTestInstance> createCasesWithJdkOnly(PluginTestContext context) throws ClassNotFoundException {
        List<PluginForkedTestInstance> instanceList = new ArrayList<>();

        if (testOnSystemClassLoader) {
            instanceList.add(new DefaultPluginForkedTestInstance(context, "", Collections.emptyList(), true));
        } else {
            instanceList.add(new DefaultPluginForkedTestInstance(context, "", Collections.emptyList(), false));
        }
        return instanceList;
    }

    boolean isOnSystemClassLoader() {
        if (testOnSystemClassLoader) {
            return true;
        }
        if (testOnChildClassLoader) {
            return false;
        }
        return false;
    }
}

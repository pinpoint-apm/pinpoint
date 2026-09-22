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

import com.navercorp.pinpoint.test.plugin.api.ClassLoading;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.OnClassLoader;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import com.navercorp.pinpoint.test.plugin.api.TestRoot;
import com.navercorp.pinpoint.test.plugin.maven.DependencyResolver;
import com.navercorp.pinpoint.test.plugin.maven.DependencyResolverFactory;
import com.navercorp.pinpoint.test.plugin.maven.DependencyVersionFilter;
import com.navercorp.pinpoint.test.plugin.shared.SharedProcessManager;
import com.navercorp.pinpoint.test.plugin.util.ArrayUtils;
import com.navercorp.pinpoint.test.plugin.util.FileUtils;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.tinylog.TaggedLogger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * We have referred OrderedThreadPoolExecutor ParentRunner of JUnit.
 *
 * @author Jongho Moon
 * @author Taejin Koo
 */
public class DefaultPluginForkedTestSuite extends AbstractPluginForkedTestSuite {
    private static final DependencyVersionFilter DEPENDENCY_VERSION_FILTER = new DependencyVersionFilter();

    private final TaggedLogger logger = TestLogger.getLogger();

    // shared by every suite of the engine: it owns the maven repository system and its session caches.
    private final DependencyResolverFactory resolverFactory;

    private final ClassLoading classLoading;

    private final String[] dependencies;
    private final Class<?> sharedClass;
    private final String[] sharedDependencies;

    private final String libraryPath;
    private final String[] librarySubDirs;

    private final boolean sharedProcess;

    public DefaultPluginForkedTestSuite(Class<?> testClass, DependencyResolverFactory resolverFactory) {
        this(testClass, true, resolverFactory);
    }

    public DefaultPluginForkedTestSuite(Class<?> testClass, boolean sharedProcess, DependencyResolverFactory resolverFactory) {
        super(testClass);
        this.resolverFactory = Objects.requireNonNull(resolverFactory, "resolverFactory");

        OnClassLoader onClassLoader = testClass.getAnnotation(OnClassLoader.class);
        this.classLoading = resolver.getClassLoading(onClassLoader);

        Dependency deps = testClass.getAnnotation(Dependency.class);
        this.dependencies = resolver.getDependency(deps);

        SharedTestLifeCycleClass sharedTestLifeCycleClass = testClass.getAnnotation(SharedTestLifeCycleClass.class);
        this.sharedClass = resolver.getSharedTestLifeCycleClass(sharedTestLifeCycleClass);

        SharedDependency sharedDeps = testClass.getAnnotation(SharedDependency.class);
        this.sharedDependencies = resolver.getSharedDependency(sharedDeps);

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
        this.sharedProcess = sharedProcess;
    }

    @Override
    protected List<PluginForkedTestInstance> createTestCases(PluginForkedTestContext context) {
        return createSharedCasesWithDependencies(context);
    }

    private List<PluginForkedTestInstance> createSharedCasesWithDependencies(PluginForkedTestContext context) {
        DependencyResolver resolver = getDependencyResolver(context.getRepositoryUrls());
        List<Path> sharedLibs = new ArrayList<>();
        sharedLibs.add(context.getTestClassLocationPath());
        sharedLibs.addAll(FileUtils.toPaths(context.getSharedLibraries()));
        if (ArrayUtils.hasLength(sharedDependencies)) {
            Map<String, List<Artifact>> dependencyMap = resolver.resolveDependencySets(sharedDependencies);
            for (Map.Entry<String, List<Artifact>> artifactEntry : dependencyMap.entrySet()) {
                final String testId = artifactEntry.getKey();
                final List<Artifact> artifacts = artifactEntry.getValue();
                try {
                    sharedLibs.addAll(resolveArtifactsAndDependencies(resolver, artifacts));
                } catch (DependencyResolutionException ex) {
                    logger.warn(ex, "resolveArtifactsAndDependencies failed testId={}", testId);
                }
            }
        }
        final String sharedClassName = sharedClass == null ? null : sharedClass.getName();
        SharedProcessManager sharedProcessManager = new SharedProcessManager(context, sharedClassName, sharedLibs);

        Map<String, List<Artifact>> dependencyMap = resolver.resolveDependencySets(DEPENDENCY_VERSION_FILTER, dependencies);
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, List<Artifact>> entry : dependencyMap.entrySet()) {
                logger.debug("{} {}", entry.getKey(), entry.getValue());
            }
        }
        List<PluginForkedTestInstance> cases = new ArrayList<>();
        for (Map.Entry<String, List<Artifact>> artifactEntry : dependencyMap.entrySet()) {
            final String testId = artifactEntry.getKey();
            final List<Artifact> artifacts = artifactEntry.getValue();

            List<Path> libs = null;
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

    private List<Path> resolveArtifactsAndDependencies(DependencyResolver resolver, List<Artifact> artifacts) throws DependencyResolutionException {
        final List<Path> files = resolver.resolveArtifactsAndDependencies(artifacts);
        return FileUtils.toAbsolutePath(files);
    }

    private DependencyResolver getDependencyResolver(List<String> repositoryUrls) {
        return this.resolverFactory.get(repositoryUrls);
    }

    private PluginForkedTestInstance newSharedProcessPluginTestCase(PluginForkedTestContext context, String testId, List<Path> libs, SharedProcessManager sharedProcessManager) {
        if (classLoading == ClassLoading.System) {
            return new SharedPluginForkedTestInstance(context, testId, libs, true, sharedProcessManager);
        }
        return new SharedPluginForkedTestInstance(context, testId, libs, false, sharedProcessManager);
    }
}

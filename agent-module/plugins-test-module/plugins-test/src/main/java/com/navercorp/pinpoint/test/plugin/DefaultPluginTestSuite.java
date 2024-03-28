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

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.classloader.PluginAgentTestClassLoader;
import com.navercorp.pinpoint.test.plugin.shared.PluginSharedInstance;
import com.navercorp.pinpoint.test.plugin.shared.PluginSharedInstanceFactory;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import com.navercorp.pinpoint.test.plugin.util.FileUtils;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import com.navercorp.pinpoint.test.plugin.util.URLUtils;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.net.URL;
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
public class DefaultPluginTestSuite extends AbstractPluginTestSuite {
    private static final Map<String, Object> RESOLVER_OPTION = createResolverOption();
    private static final DependencyResolverFactory RESOLVER_FACTORY = new DependencyResolverFactory(RESOLVER_OPTION);
    private final TaggedLogger logger = TestLogger.getLogger();
    private final boolean testOnSystemClassLoader;
    private final boolean testOnChildClassLoader;
    private final String[] repositories;
    private final String[] dependencies;
    private final Class<?> sharedClass;
    private final String[] sharedDependencies;
    private final String testClassName;

    private static Map<String, Object> createResolverOption() {
        Map<String, Object> resolverOption = new HashMap<>();
        resolverOption.put(ConfigurationProperties.CONNECT_TIMEOUT, TimeUnit.SECONDS.toMillis(5));
        resolverOption.put(ConfigurationProperties.REQUEST_TIMEOUT, TimeUnit.MINUTES.toMillis(5));
        return resolverOption;
    }

    public DefaultPluginTestSuite(Class<?> testClass) {
        this(testClass, false);
    }

    public DefaultPluginTestSuite(Class<?> testClass, boolean sharedProcess) {
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

        Repository repos = testClass.getAnnotation(Repository.class);
        this.repositories = repos == null ? new String[0] : repos.value();

        SharedTestLifeCycleClass sharedTestLifeCycleClass = testClass.getAnnotation(SharedTestLifeCycleClass.class);
        this.sharedClass = sharedTestLifeCycleClass == null ? null : sharedTestLifeCycleClass.value();

        SharedDependency sharedDependency = testClass.getAnnotation(SharedDependency.class);
        this.sharedDependencies = sharedDependency == null ? new String[0] : sharedDependency.value();
        this.testClassName = testClass.getName();
    }

    @Override
    public PluginSharedInstance createSharedInstance(PluginTestContext context) {
        if (sharedClass == null) {
            return null;
        }

        final List<String> libs = new ArrayList<>();
        libs.add(context.getTestClassLocation());
        libs.addAll(context.getSharedLibList());

        if (sharedDependencies != null && sharedDependencies.length > 0) {
            final DependencyResolver resolver = getDependencyResolver(repositories);
            final Map<String, List<Artifact>> dependencyCases = resolver.resolveDependencySets(sharedDependencies);

            for (Map.Entry<String, List<Artifact>> dependencyCase : dependencyCases.entrySet()) {
                final String testId = dependencyCase.getKey();
                try {
                    final List<Artifact> artifactList = dependencyCase.getValue();
                    libs.addAll(resolveArtifactsAndDependencies(resolver, artifactList));
                } catch (DependencyResolutionException e) {
                    logger.info("Failed to resolve artifacts and dependencies. dependency={}", dependencyCase, e);
                }
            }
        }

        PluginSharedInstanceFactory factory = new PluginSharedInstanceFactory();
        try {
            return factory.create(testClassName, sharedClass.getName(), libs);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<PluginTestInstance> createTestCases(PluginTestContext context) throws ClassNotFoundException {
        if (dependencies != null) {
            return createCasesWithDependencies(context);
        }
        return createCasesWithJdkOnly(context);
    }

    private List<PluginTestInstance> createCasesWithDependencies(PluginTestContext context) throws ClassNotFoundException {
        final PluginTestInstanceFactory pluginTestInstanceFactory = new PluginTestInstanceFactory(context);
        final List<PluginTestInstance> pluginTestInstanceList = new ArrayList<>();
        final DependencyResolver resolver = getDependencyResolver(repositories);

        final Map<String, List<Artifact>> agentDependency = resolver.resolveDependencySets("com.navercorp.pinpoint:pinpoint-plugins-test:" + Version.VERSION);
        final List<String> agentLibs = new ArrayList<>();
        agentLibs.addAll(context.getAgentLibList());

        for (Map.Entry<String, List<Artifact>> dependencyCase : agentDependency.entrySet()) {
            try {
                final List<Artifact> artifactList = dependencyCase.getValue();
                agentLibs.addAll(resolveArtifactsAndDependencies(resolver, artifactList));
            } catch (DependencyResolutionException e) {
                logger.info("Failed to resolve artifacts and dependencies. dependency={}", dependencyCase);
                return pluginTestInstanceList;
            }
        }

        final Map<String, List<Artifact>> dependencyCases = resolver.resolveDependencySets(dependencies);
        for (Map.Entry<String, List<Artifact>> dependencyCase : dependencyCases.entrySet()) {
            final String testId = dependencyCase.getKey();
            final List<String> libs = new ArrayList<>();
            try {
                final List<Artifact> artifactList = dependencyCase.getValue();
                libs.addAll(resolveArtifactsAndDependencies(resolver, artifactList));
            } catch (DependencyResolutionException e) {
                logger.info("Failed to resolve artifacts and dependencies. dependency={}", dependencyCase);
                continue;
            }

            final List<File> fileList = new ArrayList<>();
            for (String classPath : agentLibs) {
                File file = new File(classPath);
                fileList.add(file);
            }
            final URL[] agentUrls = URLUtils.fileToUrls(fileList);
            final PluginAgentTestClassLoader agentClassLoader = new PluginAgentTestClassLoader(agentUrls, Thread.currentThread().getContextClassLoader());
            agentClassLoader.setTransformIncludeList(context.getTransformIncludeList());
            Thread thread = Thread.currentThread();
            ClassLoader currentClassLoader = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader(agentClassLoader);
                final PluginTestInstance pluginTestInstance = pluginTestInstanceFactory.create(currentClassLoader, testId, agentClassLoader, libs, context.getTransformIncludeList(), isOnSystemClassLoader());
                pluginTestInstanceList.add(pluginTestInstance);
            } finally {
                thread.setContextClassLoader(currentClassLoader);
            }
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

    private List<PluginTestInstance> createCasesWithJdkOnly(PluginTestContext context) throws ClassNotFoundException {
        final PluginTestInstanceFactory pluginTestInstanceFactory = new PluginTestInstanceFactory(context);
        final PluginTestInstance pluginTestInstance = pluginTestInstanceFactory.create(Thread.currentThread().getContextClassLoader(), "", null, Collections.emptyList(), Collections.emptyList(), isOnSystemClassLoader());
        final List<PluginTestInstance> pluginTestInstanceList = new ArrayList<>();
        pluginTestInstanceList.add(pluginTestInstance);
        return pluginTestInstanceList;
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

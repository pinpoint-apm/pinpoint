/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.test.plugin.maven;

import com.navercorp.pinpoint.test.plugin.ConfigResolver;
import com.navercorp.pinpoint.test.plugin.util.ChildFirstClassLoader;
import com.navercorp.pinpoint.test.plugin.util.URLUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Loads the maven resolver from its distribution directory into a child-first class loader, the way the agent
 * is loaded from its directory: nothing of it is on a test class path, so the test class loaders, the agent
 * class loader and the forked test JVM never see the maven stack.
 * <p>
 * The distribution is a directory with {@value #RESOLVER_JAR} at its root and the runtime dependencies under
 * {@code lib/}. It is found through the {@value #PATH_PROPERTY} system property or, by default, in the working
 * directory or one of its parents, see {@link #DEFAULT_LOCATIONS}.
 * <p>
 * The resolver module compiles against this module, so its implementation finds {@link DependencyResolver}
 * and {@link DependencyResolverFactory} through the parent and the returned objects are plain instances of
 * these interfaces. Every call runs with the resolver class loader as the thread context class loader.
 */
public final class DependencyResolverFactoryLoader {

    public static final String FACTORY_CLASS = "com.navercorp.pinpoint.test.plugin.maven.resolver.MavenDependencyResolverFactory";

    public static final String PATH_PROPERTY = "pinpoint.plugins.maven.resolver.path";

    // the directory the dist zip is unpacked into by checkouts without the resolver module
    public static final String DISTRIBUTION_DIR = "pinpoint-maven-resolver";

    public static final String RESOLVER_JAR = "pinpoint-plugins-maven-resolver.jar";

    public static final String LIB_DIR = "lib";

    // looked up in the working directory and its parents, in this order: the unpacked dist zip (one per checkout,
    // in a common parent of the plugin IT modules), then the build output of the resolver module itself in a
    // pinpoint checkout. The same rule as the agent directory, agent/target/pinpoint-agent-<version>: no copy
    // per IT module and no option for an IDE run.
    static final List<Path> DEFAULT_LOCATIONS = Collections.unmodifiableList(Arrays.asList(
            Paths.get("target", DISTRIBUTION_DIR),
            Paths.get("agent-module", "plugins-test-module", "plugins-maven-resolver", "target", "dist")
    ));

    private DependencyResolverFactoryLoader() {
    }

    /**
     * The class path of the resolver class loader: the resolver jar, then the jars under {@value #LIB_DIR}.
     */
    public static List<String> findClassPaths() {
        final Path distribution = findDistribution();
        final List<String> result = new ArrayList<>(32);
        result.addAll(listJars(distribution));
        result.addAll(listJars(distribution.resolve(LIB_DIR)));
        if (result.isEmpty()) {
            throw new IllegalStateException("no jar in the pinpoint-plugins-maven-resolver distribution " + distribution);
        }
        return result;
    }

    public static Path findDistribution() {
        final String property = System.getProperty(PATH_PROPERTY);
        if (property != null) {
            final Path path = Paths.get(property).toAbsolutePath();
            if (!Files.isDirectory(path)) {
                throw new IllegalStateException("-D" + PATH_PROPERTY + "=" + property + " is not a directory");
            }
            return path;
        }
        final Path workDir = ConfigResolver.workDir().toPath();
        for (Path relativePath : DEFAULT_LOCATIONS) {
            final Path found = findUpwards(workDir, relativePath);
            if (found != null) {
                return found;
            }
        }
        throw new IllegalStateException("Cannot find the pinpoint-plugins-maven-resolver distribution: tried " + DEFAULT_LOCATIONS
                + " in " + workDir + " and its parents. Build the plugins-maven-resolver module (or the plugin IT module, which unpacks it) or set -D" + PATH_PROPERTY);
    }

    // like ConfigResolver.resolveAgentPath: a relative path looked up in the working directory and its parents
    private static Path findUpwards(Path start, Path relativePath) {
        Path parent = start;
        while (parent != null) {
            final Path candidate = parent.resolve(relativePath);
            if (Files.isDirectory(candidate)) {
                return candidate.toAbsolutePath();
            }
            parent = parent.getParent();
        }
        return null;
    }

    private static List<String> listJars(Path dir) {
        if (!Files.isDirectory(dir)) {
            return Collections.emptyList();
        }
        final List<String> jars = new ArrayList<>(32);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
            for (Path jar : stream) {
                jars.add(jar.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list " + dir, e);
        }
        Collections.sort(jars);
        return jars;
    }

    public static ClassLoader newClassLoader(List<String> classPaths, ClassLoader parent) {
        final URL[] urls = URLUtils.fileToUrls(classPaths.toArray(new String[0]));
        return new ChildFirstClassLoader(urls, parent);
    }

    public static DependencyResolverFactory load(ClassLoader resolverClassLoader, Map<String, Object> sessionConfig) {
        Objects.requireNonNull(resolverClassLoader, "resolverClassLoader");
        Objects.requireNonNull(sessionConfig, "sessionConfig");
        final DependencyResolverFactory factory = call(resolverClassLoader, () -> newFactory(resolverClassLoader, sessionConfig));
        return new ThreadContextFactory(resolverClassLoader, factory);
    }

    private static DependencyResolverFactory newFactory(ClassLoader classLoader, Map<String, Object> sessionConfig) throws ReflectiveOperationException {
        final Class<?> factoryClass = classLoader.loadClass(FACTORY_CLASS);
        if (!DependencyResolverFactory.class.isAssignableFrom(factoryClass)) {
            throw new IllegalStateException(FACTORY_CLASS + " of " + classLoader + " does not implement " + DependencyResolverFactory.class.getName()
                    + ", the resolver class loader must not contain this module");
        }
        final Constructor<?> constructor = factoryClass.getConstructor(Map.class);
        return (DependencyResolverFactory) constructor.newInstance(sessionConfig);
    }

    private static <T> T call(ClassLoader classLoader, Callable<T> callable) {
        final Thread thread = Thread.currentThread();
        final ClassLoader before = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("maven resolver call failed", e);
        } finally {
            thread.setContextClassLoader(before);
        }
    }

    private static <T> T resolve(ClassLoader classLoader, ResolveCallable<T> callable) throws DependencyResolveException {
        final Thread thread = Thread.currentThread();
        final ClassLoader before = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        try {
            return callable.call();
        } finally {
            thread.setContextClassLoader(before);
        }
    }

    private interface ResolveCallable<T> {
        T call() throws DependencyResolveException;
    }

    private static class ThreadContextFactory implements DependencyResolverFactory {
        private final ClassLoader classLoader;
        private final DependencyResolverFactory delegate;

        ThreadContextFactory(ClassLoader classLoader, DependencyResolverFactory delegate) {
            this.classLoader = classLoader;
            this.delegate = delegate;
        }

        @Override
        public DependencyResolver get(List<String> repositoryUrls) {
            final DependencyResolver resolver = call(classLoader, () -> delegate.get(repositoryUrls));
            return new ThreadContextResolver(classLoader, resolver);
        }

        @Override
        public DependencyResolver get(String... repositoryUrls) {
            final DependencyResolver resolver = call(classLoader, () -> delegate.get(repositoryUrls));
            return new ThreadContextResolver(classLoader, resolver);
        }
    }

    private static class ThreadContextResolver implements DependencyResolver {
        private final ClassLoader classLoader;
        private final DependencyResolver delegate;

        ThreadContextResolver(ClassLoader classLoader, DependencyResolver delegate) {
            this.classLoader = classLoader;
            this.delegate = delegate;
        }

        @Override
        public Map<String, List<MavenArtifact>> resolveDependencySets(String... dependencies) {
            return call(classLoader, () -> delegate.resolveDependencySets(dependencies));
        }

        @Override
        public Map<String, List<MavenArtifact>> resolveDependencySets(Predicate<String> versionFilter, String... dependencies) {
            return call(classLoader, () -> delegate.resolveDependencySets(versionFilter, dependencies));
        }

        @Override
        public List<Path> resolveArtifactsAndDependencies(List<MavenArtifact> artifacts) throws DependencyResolveException {
            return resolve(classLoader, () -> delegate.resolveArtifactsAndDependencies(artifacts));
        }

        @Override
        public List<Path> resolveArtifactsAndDependencies(String... coordinates) throws DependencyResolveException {
            return resolve(classLoader, () -> delegate.resolveArtifactsAndDependencies(coordinates));
        }
    }
}

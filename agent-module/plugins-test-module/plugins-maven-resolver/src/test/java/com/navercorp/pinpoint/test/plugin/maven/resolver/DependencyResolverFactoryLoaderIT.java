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

package com.navercorp.pinpoint.test.plugin.maven.resolver;

import com.navercorp.pinpoint.test.plugin.maven.MavenArtifact;
import com.navercorp.pinpoint.test.plugin.maven.DependencyResolver;
import com.navercorp.pinpoint.test.plugin.maven.DependencyResolverFactory;
import com.navercorp.pinpoint.test.plugin.maven.DependencyResolverFactoryLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the resolver the way the test engine and the forked launcher do: the assembled distribution
 * (resolver jar + lib/) in a child-first class loader, the contract and plugins-test in the parent.
 * Needs the packaged distribution, hence an integration test; the pom points the loader at it.
 */
public class DependencyResolverFactoryLoaderIT {

    @Test
    public void load_resolver_from_the_distribution() throws Exception {
        Path distribution = DependencyResolverFactoryLoader.findDistribution();
        assertThat(distribution.getFileName().toString()).isEqualTo("dist");

        List<String> classPaths = DependencyResolverFactoryLoader.findClassPaths();
        // the resolver jar first, then lib/*.jar; nothing from plugins-test
        assertThat(Paths.get(classPaths.get(0)).getFileName().toString()).isEqualTo(DependencyResolverFactoryLoader.RESOLVER_JAR);
        assertThat(Paths.get(classPaths.get(0)).getParent()).isEqualTo(distribution);
        List<String> libs = classPaths.subList(1, classPaths.size());
        assertThat(libs).isNotEmpty().allSatisfy(path -> assertThat(Paths.get(path).getParent()).isEqualTo(distribution.resolve(DependencyResolverFactoryLoader.LIB_DIR)));
        assertThat(libs).anyMatch(path -> path.endsWith("maven-resolver-api-1.9.27.jar"))
                .anyMatch(path -> path.endsWith("httpclient-4.5.14.jar"))
                .anyMatch(path -> path.endsWith("slf4j-tinylog-2.7.0.jar"))
                .noneMatch(path -> path.contains("pinpoint-plugins-test"))
                // tinylog-api is shared with the parent loader through TestLogger
                .noneMatch(path -> path.contains("tinylog-api"));

        ClassLoader parent = getClass().getClassLoader();
        ClassLoader resolverClassLoader = DependencyResolverFactoryLoader.newClassLoader(classPaths, parent);
        DependencyResolverFactory factory = DependencyResolverFactoryLoader.load(resolverClassLoader, Collections.emptyMap());
        DependencyResolver resolver = factory.get(Collections.emptyList());

        // the resolver and the maven stack live in the child loader, the contract is shared with the parent
        assertThat(resolverClassLoader.loadClass(DependencyResolverFactoryLoader.FACTORY_CLASS).getClassLoader()).isSameAs(resolverClassLoader);
        assertThat(resolverClassLoader.loadClass(DependencyResolver.class.getName())).isSameAs(DependencyResolver.class);
        assertThat(resolverClassLoader.loadClass(MavenArtifact.class.getName())).isSameAs(MavenArtifact.class);
        String[] resolverDependencies = {
                "org.eclipse.aether.RepositorySystem",
                "org.apache.maven.model.Model",
                "org.apache.maven.model.building.ModelBuilder",
                "org.apache.maven.artifact.versioning.ComparableVersion",
                "org.apache.maven.artifact.repository.metadata.Metadata",
                "org.apache.maven.repository.internal.MavenRepositorySystemUtils",
                "org.codehaus.plexus.util.StringUtils",
                "org.codehaus.plexus.interpolation.Interpolator",
                "javax.inject.Inject",
                "org.eclipse.sisu.Nullable",
                "org.objectweb.asm.ClassVisitor",
                "org.apache.http.client.HttpClient",
                "org.apache.http.HttpRequest",
                "org.apache.commons.codec.binary.Hex",
                "org.apache.commons.logging.LogFactory",
                "com.google.gson.Gson",
                "org.slf4j.LoggerFactory",
                "org.tinylog.slf4j.TinylogSlf4jServiceProvider",
        };
        for (String className : resolverDependencies) {
            assertThat(resolverClassLoader.loadClass(className).getClassLoader()).as(className).isSameAs(resolverClassLoader);
        }

        List<Path> files = resolver.resolveArtifactsAndDependencies("commons-logging:commons-logging:1.2");
        assertThat(files).hasSize(1);

        Map<String, List<MavenArtifact>> sets = resolver.resolveDependencySets("commons-logging:commons-logging:[1.2],[1.3.4]");
        assertThat(sets).containsOnlyKeys("commons-logging-1.2", "commons-logging-1.3.4");
        MavenArtifact coordinate = sets.get("commons-logging-1.2").get(0);
        assertThat(coordinate.getClass().getClassLoader()).as("the implementation lives in the resolver loader").isSameAs(resolverClassLoader);
        assertThat(coordinate.toString()).isEqualTo("commons-logging:commons-logging:jar:1.2");
        assertThat(resolver.resolveArtifactsAndDependencies(sets.get("commons-logging-1.2"))).isEqualTo(files);

        // the extension and the classifier survive the boundary
        Map<String, List<MavenArtifact>> classified = resolver.resolveDependencySets("net.sf.json-lib:json-lib:jar:jdk15:2.4");
        MavenArtifact jsonLib = classified.get("").get(0);
        assertThat(jsonLib.getExtension()).isEqualTo("jar");
        assertThat(jsonLib.getClassifier()).isEqualTo("jdk15");
        assertThat(jsonLib.toString()).isEqualTo("net.sf.json-lib:json-lib:jar:jdk15:2.4");
    }
}

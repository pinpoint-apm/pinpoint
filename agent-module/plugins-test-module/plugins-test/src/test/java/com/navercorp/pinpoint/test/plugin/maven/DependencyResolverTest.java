/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.test.plugin.maven;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Jongho Moon
 */
public class DependencyResolverTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void transporterFactories_supportRemote() {
        Map<String, TransporterFactory> remote = new DependencyResolver.PinpointRepositorySystemSupplier(true)
                .getTransporterFactories(Collections.emptyMap());
        Assertions.assertThat(remote).containsKeys(FileTransporterFactory.NAME, HttpTransporterFactory.NAME);

        Map<String, TransporterFactory> local = new DependencyResolver.PinpointRepositorySystemSupplier(false)
                .getTransporterFactories(Collections.emptyMap());
        Assertions.assertThat(local).containsKey(FileTransporterFactory.NAME);
        Assertions.assertThat(local).doesNotContainKey(HttpTransporterFactory.NAME);
    }

    @Test
    public void resolveArtifactsAndDependencies_includes_transitive_dependencies() throws DependencyResolutionException {
        DependencyResolverFactory factory = new DependencyResolverFactory();
        DependencyResolver resolver = factory.get();

        // maven-resolver-util depends on maven-resolver-api; the resolver-provider POMs carry <jdk> profiles,
        // so this also fails when the session lacks the JVM system properties.
        List<Path> files = resolver.resolveArtifactsAndDependencies("org.apache.maven.resolver:maven-resolver-util:1.9.27");

        Assertions.assertThat(files).hasSize(2);
        Assertions.assertThat(files).anySatisfy(path -> Assertions.assertThat(path.getFileName().toString()).isEqualTo("maven-resolver-api-1.9.27.jar"));
    }

    @Test
    public void test() {
        DependencyResolverFactory factory = new DependencyResolverFactory();
        DependencyResolver resolver = factory.get();
        resolver.resolveDependencySets("junit:junit:[4.12,4.13)");
    }

    @Test
    public void testClassifier() {
        DependencyResolverFactory factory = new DependencyResolverFactory();
        DependencyResolver resolver = factory.get();
        Map<String, List<Artifact>> sets = resolver.resolveDependencySets("net.sf.json-lib:json-lib:jar:jdk15:2.4");
        Assertions.assertThat(sets).isNotEmpty();
    }

    @Test
    public void resolveArtifactsAndDependencies() throws DependencyResolutionException, ArtifactResolutionException {
        DependencyResolverFactory factory = new DependencyResolverFactory();
        DependencyResolver resolver = factory.get();

        Map<String, List<Artifact>> sets = resolver.resolveDependencySets("org.apache.maven.resolver:maven-resolver-util:[1.0,)",
                "org.apache.maven.resolver:maven-resolver-api");

        int i = 0;
        for (Map.Entry<String, List<Artifact>> set : sets.entrySet()) {
            logger.debug("{}", i++);
            List<Path> results = resolver.resolveArtifactsAndDependencies(set.getValue());

            logger.debug(set.getKey());

            for (Path result : results) {
                logger.debug("{}", result);
            }
        }
    }

}

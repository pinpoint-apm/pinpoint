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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Resolves the maven artifacts of a plugin test.
 * <p>
 * The implementation lives in the pinpoint-plugins-maven-resolver module and is loaded through
 * {@link DependencyResolverFactoryLoader} in a child class loader, so this contract only uses JDK types and
 * {@link MavenArtifact}: the aether types and their transitive dependencies never reach the test or the
 * agent class loader.
 */
public interface DependencyResolver {

    /**
     * Expands the version ranges of the dependencies into one artifact set per test case.
     *
     * @return testId to artifact coordinates, in declaration order
     */
    Map<String, List<MavenArtifact>> resolveDependencySets(String... dependencies);

    Map<String, List<MavenArtifact>> resolveDependencySets(Predicate<String> versionFilter, String... dependencies);

    /**
     * @return the files of the artifacts and of their runtime dependencies
     */
    List<Path> resolveArtifactsAndDependencies(List<MavenArtifact> artifacts) throws DependencyResolveException;

    /**
     * @param coordinates aether coordinates, {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}
     */
    List<Path> resolveArtifactsAndDependencies(String... coordinates) throws DependencyResolveException;
}

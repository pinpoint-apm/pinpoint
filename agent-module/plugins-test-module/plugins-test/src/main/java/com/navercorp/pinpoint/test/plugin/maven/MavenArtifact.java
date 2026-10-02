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

/**
 * The identity of a maven artifact, the counterpart of {@code org.eclipse.aether.artifact.Artifact} that is
 * allowed to cross the resolver class loader boundary.
 * <p>
 * Instances are only created by the resolver, see {@link DependencyResolver#resolveDependencySets(String...)};
 * the engine passes them back untouched or writes them to the forked test JVM command line. For that,
 * {@link #toString()} must return the aether coordinate form
 * {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>} that
 * {@link DependencyResolver#resolveArtifactsAndDependencies(String...)} accepts again.
 * {@link #equals(Object)} compares the five coordinate fields and nothing else.
 */
public interface MavenArtifact {

    String getGroupId();

    String getArtifactId();

    /**
     * @return never empty, {@code jar} by default
     */
    String getExtension();

    /**
     * @return empty when the artifact has no classifier
     */
    String getClassifier();

    String getVersion();
}

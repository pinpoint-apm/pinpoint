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
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts between aether artifacts and {@link MavenArtifact}, the only artifact type that crosses the
 * resolver class loader boundary.
 */
public final class ArtifactConverter {

    private ArtifactConverter() {
    }

    public static Artifact toArtifact(MavenArtifact coordinate) {
        if (coordinate instanceof AetherArtifact) {
            return ((AetherArtifact) coordinate).getArtifact();
        }
        return new DefaultArtifact(coordinate.getGroupId(), coordinate.getArtifactId(), coordinate.getClassifier(), coordinate.getExtension(), coordinate.getVersion());
    }

    public static List<Artifact> toArtifacts(List<MavenArtifact> coordinates) {
        List<Artifact> result = new ArrayList<>(coordinates.size());
        for (MavenArtifact coordinate : coordinates) {
            result.add(toArtifact(coordinate));
        }
        return result;
    }

    /**
     * @param coordinates aether coordinates, parsed by {@link DefaultArtifact#DefaultArtifact(String)}
     * @throws IllegalArgumentException on a malformed coordinate
     */
    public static List<Artifact> toArtifacts(String... coordinates) {
        List<Artifact> result = new ArrayList<>(coordinates.length);
        for (String coordinate : coordinates) {
            result.add(new DefaultArtifact(coordinate));
        }
        return result;
    }

    public static MavenArtifact toMavenArtifact(Artifact artifact) {
        return new AetherArtifact(artifact);
    }

    public static List<MavenArtifact> toMavenArtifacts(List<Artifact> artifacts) {
        List<MavenArtifact> result = new ArrayList<>(artifacts.size());
        for (Artifact artifact : artifacts) {
            result.add(toMavenArtifact(artifact));
        }
        return result;
    }
}

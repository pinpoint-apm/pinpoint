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

import java.util.Objects;

/**
 * {@link MavenArtifact} backed by an aether {@link Artifact}.
 * <p>
 * The getters and {@link #toString()} delegate to the artifact, so the text form is whatever aether writes and
 * reads, {@code groupId:artifactId:extension[:classifier]:version}. Equality is the five coordinate fields only:
 * {@code AbstractArtifact.equals} also compares the resolved file and the properties, which would make the same
 * coordinate differ before and after resolution.
 */
public final class AetherArtifact implements MavenArtifact {

    private final Artifact artifact;

    public AetherArtifact(Artifact artifact) {
        this.artifact = Objects.requireNonNull(artifact, "artifact");
    }

    public Artifact getArtifact() {
        return artifact;
    }

    @Override
    public String getGroupId() {
        return artifact.getGroupId();
    }

    @Override
    public String getArtifactId() {
        return artifact.getArtifactId();
    }

    @Override
    public String getExtension() {
        return artifact.getExtension();
    }

    @Override
    public String getClassifier() {
        return artifact.getClassifier();
    }

    @Override
    public String getVersion() {
        return artifact.getVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MavenArtifact)) {
            return false;
        }
        MavenArtifact that = (MavenArtifact) o;
        return getGroupId().equals(that.getGroupId())
                && getArtifactId().equals(that.getArtifactId())
                && getExtension().equals(that.getExtension())
                && getClassifier().equals(that.getClassifier())
                && getVersion().equals(that.getVersion());
    }

    @Override
    public int hashCode() {
        int result = getGroupId().hashCode();
        result = 31 * result + getArtifactId().hashCode();
        result = 31 * result + getExtension().hashCode();
        result = 31 * result + getClassifier().hashCode();
        result = 31 * result + getVersion().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return artifact.toString();
    }
}

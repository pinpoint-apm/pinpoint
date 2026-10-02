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
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AetherArtifactTest {

    @Test
    public void delegates_to_the_artifact() {
        MavenArtifact c = ArtifactConverter.toMavenArtifact(new DefaultArtifact("net.sf.json-lib:json-lib:jar:jdk15:2.4"));
        assertThat(c.getGroupId()).isEqualTo("net.sf.json-lib");
        assertThat(c.getArtifactId()).isEqualTo("json-lib");
        assertThat(c.getExtension()).isEqualTo("jar");
        assertThat(c.getClassifier()).isEqualTo("jdk15");
        assertThat(c.getVersion()).isEqualTo("2.4");

        MavenArtifact plain = ArtifactConverter.toMavenArtifact(new DefaultArtifact("commons-logging:commons-logging:1.2"));
        assertThat(plain.getExtension()).isEqualTo("jar");
        assertThat(plain.getClassifier()).isEmpty();
    }

    @Test
    public void toString_is_read_back_by_aether() {
        String[] coordinates = {
                "g:a:1",
                "g:a:war:1",
                "g:a:jar:tests:1",
                "g:a:zip:dist:[1.0,2.0)",
                "org.example:bom:pom:1.0",
        };
        for (String coordinate : coordinates) {
            Artifact artifact = new DefaultArtifact(coordinate);
            MavenArtifact c = ArtifactConverter.toMavenArtifact(artifact);
            // the forked launcher hands c.toString() back to resolveArtifactsAndDependencies(String...)
            Artifact reparsed = ArtifactConverter.toArtifacts(c.toString()).get(0);
            assertThat(ArtifactConverter.toMavenArtifact(reparsed)).isEqualTo(c);
            assertThat(reparsed.getExtension()).isEqualTo(artifact.getExtension());
            assertThat(reparsed.getClassifier()).isEqualTo(artifact.getClassifier());
        }
    }

    @Test
    public void equals_ignores_file_and_properties() {
        Artifact unresolved = new DefaultArtifact("g:a:1");
        Artifact resolved = unresolved.setFile(new File("a-1.jar"));
        assertThat(resolved).isNotEqualTo(unresolved);

        MavenArtifact a = ArtifactConverter.toMavenArtifact(unresolved);
        MavenArtifact b = ArtifactConverter.toMavenArtifact(resolved);
        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);

        assertThat(a).isNotEqualTo(ArtifactConverter.toMavenArtifact(new DefaultArtifact("g:a:2")));
        assertThat(a).isNotEqualTo(ArtifactConverter.toMavenArtifact(new DefaultArtifact("g:a:pom:1")));
        assertThat(a).isNotEqualTo(ArtifactConverter.toMavenArtifact(new DefaultArtifact("g:a:jar:tests:1")));
    }

    @Test
    public void toArtifact_unwraps_or_rebuilds() {
        Artifact artifact = new DefaultArtifact("g:a:jar:tests:1").setFile(new File("a-1-tests.jar"));
        MavenArtifact wrapped = ArtifactConverter.toMavenArtifact(artifact);
        assertThat(ArtifactConverter.toArtifact(wrapped)).isSameAs(artifact);

        MavenArtifact foreign = new MavenArtifact() {
            public String getGroupId() { return "g"; }
            public String getArtifactId() { return "a"; }
            public String getExtension() { return "jar"; }
            public String getClassifier() { return "tests"; }
            public String getVersion() { return "1"; }
        };
        Artifact rebuilt = ArtifactConverter.toArtifact(foreign);
        assertThat(rebuilt.toString()).isEqualTo("g:a:jar:tests:1");
        assertThat(wrapped).isEqualTo(ArtifactConverter.toMavenArtifact(rebuilt));
    }

    @Test
    public void bad_coordinates_fail_in_aether() {
        assertThatThrownBy(() -> ArtifactConverter.toArtifacts("dependency1")).isInstanceOf(IllegalArgumentException.class);
    }
}

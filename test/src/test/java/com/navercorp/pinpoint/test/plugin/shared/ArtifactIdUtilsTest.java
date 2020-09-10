/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import org.eclipse.aether.artifact.Artifact;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author WonChul Heo(heowc)
 */
public class ArtifactIdUtilsTest {

    @Test
    public void testInvalidToArtifact() {
        Artifact artifact = ArtifactIdUtils.toArtifact((String) null);
        assertThat(artifact, nullValue());
        artifact = ArtifactIdUtils.toArtifact("fooBar");
        assertThat(artifact, nullValue());
        artifact = ArtifactIdUtils.toArtifact("foo:bar");
        assertThat(artifact, nullValue());
        artifact = ArtifactIdUtils.toArtifact("a:b:c:d:e");
        assertThat(artifact, nullValue());
    }

    @Test
    public void testInvalidToArtifacts() {
        List<Artifact> artifacts = ArtifactIdUtils.toArtifact((String[]) null);
        assertThat(artifacts.size(), is(0));
        artifacts = ArtifactIdUtils.toArtifact(new String[]{"fooBar"});
        assertThat(artifacts.size(), is(0));
    }

    @Test
    public void testToArtifact1() {
        final Artifact artifact = ArtifactIdUtils.toArtifact("foo:bar:1.0.0");
        assertThat(artifact, notNullValue());
        assertThat(artifact.getGroupId(), is("foo"));
        assertThat(artifact.getArtifactId(), is("bar"));
        assertThat(artifact.getBaseVersion(), is("1.0.0"));
        assertThat(artifact.getVersion(), is("1.0.0"));
        assertThat(artifact.getExtension(), is("jar"));
        assertThat(artifact.getClassifier(), is(""));
    }

    @Test
    public void testToArtifact2() {
        final Artifact artifact = ArtifactIdUtils.toArtifact("foo:bar:1.0.0:classifier");
        assertThat(artifact, notNullValue());
        assertThat(artifact.getGroupId(), is("foo"));
        assertThat(artifact.getArtifactId(), is("bar"));
        assertThat(artifact.getBaseVersion(), is("1.0.0"));
        assertThat(artifact.getVersion(), is("1.0.0"));
        assertThat(artifact.getExtension(), is("jar"));
        assertThat(artifact.getClassifier(), is("classifier"));
    }

    @Test
    public void testToArtifacts() {
        final List<Artifact> artifacts = ArtifactIdUtils.toArtifact(new String[]{ "foo:bar:1.0.0", "foo:bar:1.0.0:classifier"});
        assertThat(artifacts.size(), is(2));
    }

    @Test
    public void testArtifactToString1() {
        final String artifactName = "foo:bar:1.0.0";
        final Artifact artifact = ArtifactIdUtils.toArtifact(artifactName);
        final String actualValue = ArtifactIdUtils.artifactToString(artifact);
        assertThat(actualValue, is(artifactName));
    }

    @Test
    public void testArtifactToString2() {
        final String artifactName = "foo:bar:1.0.0:classifier";
        final Artifact artifact = ArtifactIdUtils.toArtifact(artifactName);
        final String actualValue = ArtifactIdUtils.artifactToString(artifact);
        assertThat(actualValue, is(artifactName));
    }
}

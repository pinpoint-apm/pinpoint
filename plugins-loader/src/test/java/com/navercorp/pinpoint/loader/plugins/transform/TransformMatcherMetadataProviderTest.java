/*
 * Copyright 2021 NAVER Corp.
 *
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

package com.navercorp.pinpoint.loader.plugins.transform;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.TransformMatcherMetadata;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.JarFileMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.VersionMatcherOperand;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransformMatcherMetadataProviderTest {

    @Test
    public void getTransformMatcherMetadata() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/transform-matcher.yml");
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("4.1.2.RELEASE ~ 4.3.x.RELEASE");
        assertNotNull(versionMatcherOperand);
        VersionMatcherOperand notFoundVersionMatcherOperand = metadata.getVersionMatcherOperand("not-found");
        assertNull(notFoundVersionMatcherOperand);

        JarFileMatcherOperand jarFileMatcherOperand = metadata.getJarFileMatcherOperand("3.x");
        assertNotNull(jarFileMatcherOperand);
        JarFileMatcherOperand notFoundJarFileMatcherOperand = metadata.getJarFileMatcherOperand("not-found");
        assertNull(notFoundJarFileMatcherOperand);
    }

    @Test
    public void notFoundMetadataFile() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/not-found.yml");
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("4.1.2.RELEASE ~ 4.3.x.RELEASE");
        assertNull(versionMatcherOperand);
    }

    @Test
    public void invalidFormat1() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/invalid-format-1.yml");
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("4.1.2.RELEASE ~ 4.3.x.RELEASE");
        assertNull(versionMatcherOperand);
    }

    @Test
    public void invalidFormat2() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/invalid-format-2.yml");
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("4.1.2.RELEASE ~ 4.3.x.RELEASE");
        assertNull(versionMatcherOperand);
    }

    @Test
    public void invalidFormat3() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/invalid-format-3.yml");
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("4.1.2.RELEASE ~ 4.3.x.RELEASE");
        assertNull(versionMatcherOperand);
    }

    @Test
    public void nameFieldNull() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/name-field-null.yml");
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("4.1.2.RELEASE ~ 4.3.x.RELEASE");
        assertNull(versionMatcherOperand);

        JarFileMatcherOperand jarFileMatcherOperand = metadata.getJarFileMatcherOperand("3.x");
        assertNull(jarFileMatcherOperand);
    }

    @Test
    public void rangesFieldNull() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/ranges-field-null.yml");
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("4.1.2.RELEASE ~ 4.3.x.RELEASE");
        assertNull(versionMatcherOperand);
    }

    @Test
    public void resolverFieldNull() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/resolver-field-null.yml");
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("4.1.2.RELEASE ~ 4.3.x.RELEASE");
        assertNull(versionMatcherOperand);
    }

    @Test
    public void patternsFieldNull() throws Exception {
        TransformMatcherMetadata metadata = getTransformMatcherMetadata("transform-matchers/patterns-field-null.yml");
        JarFileMatcherOperand jarFileMatcherOperand = metadata.getJarFileMatcherOperand("3.x");
        assertNull(jarFileMatcherOperand);
    }

    private TransformMatcherMetadata getTransformMatcherMetadata(final String metaFilePath) {
        TransformMatcherMetadataProvider provider = new TransformMatcherMetadataProvider(metaFilePath);
        TransformMatcherMetadata metadata = provider.getTransformMatcherMetadata(new ProfilerPlugin() {
            @Override
            public void setup(ProfilerPluginSetupContext context) {
            }
        });

        return metadata;
    }
}
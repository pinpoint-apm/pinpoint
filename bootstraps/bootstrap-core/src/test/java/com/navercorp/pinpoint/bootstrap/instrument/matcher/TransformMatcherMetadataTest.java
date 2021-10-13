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

package com.navercorp.pinpoint.bootstrap.instrument.matcher;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.JarFileMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.VersionMatcherOperand;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TransformMatcherMetadataTest {

    @Test
    public void getVersionMatcherOperand() {
        List<String> rangeList = Arrays.asList("[4.2.0.RELEASE,4.2.max]", "[4.3.0.RELEASE,4.3.max]");
        List<String> resolverList = Arrays.asList("classloader-package", "file-version", "metainf=Implementation-Version");
        List<String> patternList = Arrays.asList("antstyle:test-core-3.??", "regex:test-util-*");

        TransformMatcherMetadata.Builder builder = new TransformMatcherMetadata.Builder();
        builder.versionMatcher("A", rangeList, resolverList, false);
        builder.jarFileMatcher("B", patternList, false);

        TransformMatcherMetadata metadata = builder.build();
        VersionMatcherOperand versionMatcherOperand = metadata.getVersionMatcherOperand("A");
        assertNotNull(versionMatcherOperand);
        JarFileMatcherOperand jarFileMatcherOperand = metadata.getJarFileMatcherOperand("B");
        assertNotNull(jarFileMatcherOperand);
    }
}
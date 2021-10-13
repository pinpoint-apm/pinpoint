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
import com.navercorp.pinpoint.common.annotations.InterfaceStability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@InterfaceStability.Unstable
public class TransformMatcherMetadata {

    private final Map<String, VersionMatcherOperand> versionMatcherOperandMap;
    private final Map<String, JarFileMatcherOperand> jarFileMatcherOperandMap;

    TransformMatcherMetadata(Map<String, VersionMatcherOperand> versionMatcherOperandMap, Map<String, JarFileMatcherOperand> jarFileMatcherOperandMap) {
        this.versionMatcherOperandMap = versionMatcherOperandMap;
        this.jarFileMatcherOperandMap = jarFileMatcherOperandMap;
    }

    public VersionMatcherOperand getVersionMatcherOperand(final String name) {
        return this.versionMatcherOperandMap.get(name);
    }

    public JarFileMatcherOperand getJarFileMatcherOperand(final String name) {
        return this.jarFileMatcherOperandMap.get(name);
    }

    @Override
    public String toString() {
        return "{" +
                "versionMatcherOperandMap=" + versionMatcherOperandMap +
                '}';
    }

    public static class Builder {
        private Map<String, VersionMatcherOperand> versionMatcherOperandMap = new HashMap<>();
        private Map<String, JarFileMatcherOperand> jarFileMatcherOperandMap = new HashMap<>();

        public Builder() {
        }

        public Builder versionMatcher(final String name, final List<String> ranges, final List<String> resolvers, boolean forcedMatch) {
            final VersionMatcherOperand operand = new VersionMatcherOperand(ranges, resolvers, forcedMatch);
            versionMatcherOperandMap.put(name, operand);
            return this;
        }

        public Builder jarFileMatcher(final String name, final List<String> patterns, boolean forcedMatch) {
            final JarFileMatcherOperand operand = new JarFileMatcherOperand(patterns, forcedMatch);
            this.jarFileMatcherOperandMap.put(name, operand);
            return this;
        }

        public TransformMatcherMetadata build() {
            return new TransformMatcherMetadata(versionMatcherOperandMap, jarFileMatcherOperandMap);
        }
    }
}

/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MatcherType;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class MatchableTransformerRegistry implements TransformerRegistry {

    // class name.
    private final TransformerRegistry defaultTransformerRegistry;
    // class or package matcher operand.
    private final TransformerIndex index;

    public static Builder newBuilder(final InstrumentMatcherCacheConfig instrumentMatcherCacheConfig) {
        return new Builder(instrumentMatcherCacheConfig);
    }

    MatchableTransformerRegistry(final TransformerRegistry defaultTransformerRegistry,
                                 final TransformerIndex index) {
        this.defaultTransformerRegistry = Objects.requireNonNull(defaultTransformerRegistry, "defaultTransformerRegistry");
        this.index = Objects.requireNonNull(index, "index");
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer) {
        return findTransformer(classLoader, classInternalName, classFileBuffer, null);
    }

    @Override
    public ClassFileTransformer findTransformer(final ClassLoader classLoader, final String classInternalName, final byte[] classFileBuffer, final InternalClassMetadata classMetadata) {
        // find default.
        final ClassFileTransformer transformer = this.defaultTransformerRegistry.findTransformer(classLoader, classInternalName, classFileBuffer);
        if (transformer != null) {
            return transformer;
        }

        // find class or package name based.
        final ClassMetadataWrapper classMetadataWrapper = new ClassMetadataWrapper(classFileBuffer, classMetadata);
        return this.index.find(classLoader, classInternalName, classMetadataWrapper);
    }

    public static class Builder {
        private final Logger logger = LogManager.getLogger(this.getClass());

        private final InstrumentMatcherCacheConfig instrumentMatcherCacheConfig;
        // class name matcher, looked up by exact class name.
        private final List<MatchableClassFileTransformer> defaultTransformerList = new ArrayList<>();
        // class or package based matcher.
        private final IndexBuilder indexBuilder = new IndexBuilder();

        Builder(final InstrumentMatcherCacheConfig instrumentMatcherCacheConfig) {
            this.instrumentMatcherCacheConfig = Objects.requireNonNull(instrumentMatcherCacheConfig, "instrumentMatcherCacheConfig");
        }

        public Builder addAll(final List<MatchableClassFileTransformer> matchableClassFileTransformerList) {
            Objects.requireNonNull(matchableClassFileTransformerList, "matchableClassFileTransformerList");
            for (MatchableClassFileTransformer transformer : matchableClassFileTransformerList) {
                add(transformer);
            }
            return this;
        }

        public Builder add(final MatchableClassFileTransformer transformer) {
            Objects.requireNonNull(transformer, "transformer");
            final Matcher matcher = transformer.getMatcher();
            if (MatcherType.isBasedMatcher(matcher)) {
                try {
                    indexBuilder.add(matcher, transformer);
                } catch (Exception ex) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to add transformer {}", transformer, ex);
                    }
                }
            } else {
                defaultTransformerList.add(transformer);
            }
            return this;
        }

        public MatchableTransformerRegistry build() {
            final DefaultTransformerRegistry defaultTransformerRegistry = new DefaultTransformerRegistry(defaultTransformerList);
            final TransformerMatcher transformerMatcher = new DefaultTransformerMatcher(instrumentMatcherCacheConfig);
            final IndexValueMatcher indexValueMatcher = new IndexValueMatcher(transformerMatcher);
            // read-only from here on.
            final TransformerIndex index = indexBuilder.build(indexValueMatcher);
            return new MatchableTransformerRegistry(defaultTransformerRegistry, index);
        }
    }
}

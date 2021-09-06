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

import com.navercorp.pinpoint.profiler.instrument.config.InstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.BasedMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MatcherType;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import java.util.Objects;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadataReader;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jaehong.kim
 */
public class MatchableTransformerRegistry implements TransformerRegistry {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // class name.
    private final DefaultTransformerRegistry defaultTransformerRegistry;

    // class matcher operand.
    private final Map<String, IndexValue> classNameBasedIndex = new HashMap<String, IndexValue>(64);
    // package matcher operand.
    private final Map<String, Set<IndexValue>> packageNameBasedIndex;

    private final TransformerMatcherExecutionPlanner executionPlanner = new TransformerMatcherExecutionPlanner();
    private final TransformerMatcher transformerMatcher;

    public MatchableTransformerRegistry(InstrumentMatcherCacheConfig instrumentMatcherCacheConfig, List<MatchableClassFileTransformer> matchableClassFileTransformerList) {
        Objects.requireNonNull(instrumentMatcherCacheConfig, "instrumentMatcherCacheConfig");
        Objects.requireNonNull(matchableClassFileTransformerList, "matchableClassFileTransformerList");

        final List<MatchableClassFileTransformer> defaultTransfomerList = filterDefaultMatcher(matchableClassFileTransformerList);
        this.defaultTransformerRegistry = new DefaultTransformerRegistry(defaultTransfomerList);

        // sorted by package name length.
        this.packageNameBasedIndex = new TreeMap<String, Set<IndexValue>>(new Comparator<String>() {
            @Override
            public int compare(String key1, String key2) {
                return key1.compareTo(key2);
            }
        });

        final List<MatchableClassFileTransformer> baseTransformer = filterBaseMatcher(matchableClassFileTransformerList);
        for (MatchableClassFileTransformer transformer : baseTransformer) {
            try {
                addTransformer(transformer.getMatcher(), transformer);
            } catch (Exception ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to add transformer {}", transformer, ex);
                }
            }
        }

        this.transformerMatcher = new DefaultTransformerMatcher(instrumentMatcherCacheConfig);

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

        final ClassMetadataWrapper classMetadataWrapper = new ClassMetadataWrapper(classFileBuffer, classMetadata);
        // find class name based.
        if (!this.classNameBasedIndex.isEmpty()) {
            final ClassFileTransformer classBaseTransformer = findClassBasedTransformer(classLoader, classInternalName, classMetadataWrapper);
            if (classBaseTransformer != null) {
                return classBaseTransformer;
            }
        }

        // find package name based.
        if (!this.packageNameBasedIndex.isEmpty()) {
            final ClassFileTransformer packagedBasedTransformer = findPackageBasedTransformer(classLoader, classInternalName, classMetadataWrapper);
            if (packagedBasedTransformer != null) {
                return packagedBasedTransformer;
            }
        }

        // not found.
        return null;
    }

    private ClassFileTransformer findClassBasedTransformer(final ClassLoader classLoader, final String classInternalName, final ClassMetadataWrapper classMetadataWrapper) {
        IndexValue indexValue = this.classNameBasedIndex.get(classInternalName);
        if (indexValue != null) {
            if (indexValue.operand instanceof ClassInternalNameMatcherOperand) {
                // single operand.
                return indexValue.transformer;
            }

            ClassFileTransformer transformer = match(classLoader, indexValue, classMetadataWrapper);
            if (transformer != null) {
                return transformer;
            }
        }

        return null;
    }

    private ClassFileTransformer findPackageBasedTransformer(final ClassLoader classLoader, final String classInternalName, final ClassMetadataWrapper classMetadataWrapper) {
        for (Map.Entry<String, Set<IndexValue>> entry : this.packageNameBasedIndex.entrySet()) {
            final String packageInternalName = entry.getKey();
            if (classInternalName.startsWith(packageInternalName)) {
                for (IndexValue value : entry.getValue()) {
                    ClassFileTransformer transformer = match(classLoader, value, classMetadataWrapper);
                    if (transformer != null) {
                        return transformer;
                    }
                }
            }
        }

        return null;
    }

    private ClassFileTransformer match(final ClassLoader classLoader, final IndexValue indexValue, final ClassMetadataWrapper classMetadataWrapper) {
        final long startTime = System.currentTimeMillis();
        if (transformerMatcher.match(classLoader, indexValue.operand, classMetadataWrapper.get())) {
            long elapsedTime = indexValue.accumulatorTime(startTime);
            if (isDebug) {
                logger.debug("Matching time elapsed={}ms, accumulator={}ms, operand={}", elapsedTime, indexValue.accumulatorTimeMillis, indexValue.operand);
            }
            return indexValue.transformer;
        } else {
            indexValue.accumulatorTime(startTime);
        }

        return null;
    }

    private void addTransformer(final Matcher matcher, final ClassFileTransformer transformer) {
        if (!MatcherType.isBasedMatcher(matcher)) {
            throw new IllegalArgumentException("unsupported baseMatcher");
        }
        // class or package based.
        MatcherOperand matcherOperand = ((BasedMatcher) matcher).getMatcherOperand();
        addIndex(matcherOperand, transformer);
    }

    private List<MatchableClassFileTransformer> filterBaseMatcher(List<MatchableClassFileTransformer> matchableClassFileTransformerList) {
        // class name
        final List<MatchableClassFileTransformer> filter = new ArrayList<MatchableClassFileTransformer>();
        for (MatchableClassFileTransformer transformer : matchableClassFileTransformerList) {
            if (MatcherType.isBasedMatcher(transformer.getMatcher())) {
                filter.add(transformer);
            }
        }
        return filter;
    }

    private List<MatchableClassFileTransformer> filterDefaultMatcher(List<MatchableClassFileTransformer> matchableClassFileTransformerList) {
        // class name
        final List<MatchableClassFileTransformer> filter = new ArrayList<MatchableClassFileTransformer>();
        for (MatchableClassFileTransformer transformer : matchableClassFileTransformerList) {
            if (!MatcherType.isBasedMatcher(transformer.getMatcher())) {
                filter.add(transformer);
            }
        }
        return filter;
    }

    private void addIndex(final MatcherOperand condition, final ClassFileTransformer transformer) {
        // find class or package matcher operand.
        final List<MatcherOperand> indexedMatcherOperands = executionPlanner.findIndex(condition);
        if (indexedMatcherOperands.isEmpty()) {
            throw new IllegalArgumentException("invalid matcher - not found index operand. condition=" + condition);
        }

        boolean indexed;
        final IndexValue indexValue = new IndexValue(condition, transformer);
        for (MatcherOperand operand : indexedMatcherOperands) {
            if (operand instanceof ClassInternalNameMatcherOperand) {
                ClassInternalNameMatcherOperand classInternalNameMatcherOperand = (ClassInternalNameMatcherOperand) operand;
                final IndexValue prev = classNameBasedIndex.put(classInternalNameMatcherOperand.getClassInternalName(), indexValue);
                if (prev != null) {
                    throw new IllegalStateException("Transformer already exist. class=" + classInternalNameMatcherOperand.getClassInternalName() + ", new=" + indexValue + ", prev=" + prev);
                }
                indexed = true;
            } else if (operand instanceof PackageInternalNameMatcherOperand) {
                PackageInternalNameMatcherOperand packageInternalNameMatcherOperand = (PackageInternalNameMatcherOperand) operand;
                addIndexData(packageInternalNameMatcherOperand.getPackageInternalName(), indexValue, this.packageNameBasedIndex);
                indexed = true;
            } else {
                throw new IllegalArgumentException("invalid matcher or execution planner - unknown operand. condition=" + condition + ", unknown operand=" + operand);
            }

            if (!indexed) {
                throw new IllegalArgumentException("invalid matcher or execution planner - no such indexed operand. operand=" + condition);
            }
        }
    }

    private void addIndexData(final String key, final IndexValue indexValue, final Map<String, Set<IndexValue>> index) {
        Set<IndexValue> indexValueSet = index.get(key);
        if (indexValueSet == null) {
            indexValueSet = new HashSet<IndexValue>();
            index.put(key, indexValueSet);
        }
        indexValueSet.add(indexValue);
    }

    static class IndexValue {
        private final MatcherOperand operand;
        private final ClassFileTransformer transformer;
        private final AtomicLong accumulatorTimeMillis = new AtomicLong(0);

        public IndexValue(final MatcherOperand operand, final ClassFileTransformer transformer) {
            this.operand = operand;
            this.transformer = transformer;
        }

        public long accumulatorTime(final long startTimeMillis) {
            final long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
            accumulatorTimeMillis.addAndGet(elapsedTimeMillis);
            return elapsedTimeMillis;
        }
    }

    class ClassMetadataWrapper {
        private final byte[] classFileBuffer;
        private InternalClassMetadata classMetadata;

        ClassMetadataWrapper(final byte[] classFileBuffer, final InternalClassMetadata classMetadata) {
            this.classFileBuffer = classFileBuffer;
            this.classMetadata = classMetadata;
        }

        public InternalClassMetadata get() {
            if (this.classMetadata == null) {
                try {
                    this.classMetadata = InternalClassMetadataReader.readInternalClassMetadata(this.classFileBuffer);
                } catch (Exception e) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Failed to read metadata of class bytes.", e);
                    }
                    return null;
                }
            }

            return this.classMetadata;
        }
    }
}
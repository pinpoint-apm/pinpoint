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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.BasedMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MatcherType;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadataReader;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * @author jaehong.kim
 */
public class MatchableTransformerRegistry implements TransformerRegistry {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // class name.
    private final DefaultTransformerRegistry defaultTransformerRegistry;

    // class matcher operand.
    private final Map<String, IndexValue> classNameBasedIndex;
    // package matcher operand, scanned linearly on every class load.
    private final PackageIndex[] packageIndexes;

    private final TransformerMatcher transformerMatcher;

    public static Builder newBuilder(final InstrumentMatcherCacheConfig instrumentMatcherCacheConfig) {
        return new Builder(instrumentMatcherCacheConfig);
    }

    MatchableTransformerRegistry(final DefaultTransformerRegistry defaultTransformerRegistry,
                                 final Map<String, IndexValue> classNameBasedIndex,
                                 final PackageIndex[] packageIndexes,
                                 final TransformerMatcher transformerMatcher) {
        this.defaultTransformerRegistry = Objects.requireNonNull(defaultTransformerRegistry, "defaultTransformerRegistry");
        this.classNameBasedIndex = Objects.requireNonNull(classNameBasedIndex, "classNameBasedIndex");
        this.packageIndexes = Objects.requireNonNull(packageIndexes, "packageIndexes");
        this.transformerMatcher = Objects.requireNonNull(transformerMatcher, "transformerMatcher");
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
        if (this.packageIndexes.length > 0) {
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
        for (PackageIndex packageIndex : this.packageIndexes) {
            if (classInternalName.startsWith(packageIndex.packageInternalName)) {
                for (IndexValue value : packageIndex.values) {
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
            // read-only from here on.
            return new MatchableTransformerRegistry(defaultTransformerRegistry,
                    indexBuilder.buildClassNameBasedIndex(),
                    indexBuilder.buildPackageIndexes(),
                    transformerMatcher);
        }
    }

    static class IndexBuilder {
        private final TransformerMatcherExecutionPlanner executionPlanner = new TransformerMatcherExecutionPlanner();
        // class matcher operand.
        private final Map<String, IndexValue> classNameBasedIndex = new HashMap<>(64);
        // groups the index values by package internal name; the lookup order is decided when the index is frozen.
        private final Map<String, Set<IndexValue>> packageNameBasedIndex = new HashMap<>();

        void add(final Matcher matcher, final ClassFileTransformer transformer) {
            if (!MatcherType.isBasedMatcher(matcher)) {
                throw new IllegalArgumentException("unsupported baseMatcher");
            }
            // class or package based.
            final MatcherOperand matcherOperand = ((BasedMatcher) matcher).getMatcherOperand();
            addIndex(matcherOperand, transformer);
        }

        private void addIndex(final MatcherOperand condition, final ClassFileTransformer transformer) {
            // find class or package matcher operand.
            final List<MatcherOperand> indexedMatcherOperands = executionPlanner.findIndex(condition);
            if (indexedMatcherOperands.isEmpty()) {
                throw new IllegalArgumentException("invalid matcher - not found index operand. condition=" + condition);
            }

            final IndexValue indexValue = new IndexValue(condition, transformer);
            for (MatcherOperand operand : indexedMatcherOperands) {
                if (operand instanceof ClassInternalNameMatcherOperand) {
                    final ClassInternalNameMatcherOperand classInternalNameMatcherOperand = (ClassInternalNameMatcherOperand) operand;
                    final IndexValue prev = classNameBasedIndex.put(classInternalNameMatcherOperand.getClassInternalName(), indexValue);
                    if (prev != null) {
                        throw new IllegalStateException("Transformer already exist. class=" + classInternalNameMatcherOperand.getClassInternalName() + ", new=" + indexValue + ", prev=" + prev);
                    }
                } else if (operand instanceof PackageInternalNameMatcherOperand) {
                    final PackageInternalNameMatcherOperand packageInternalNameMatcherOperand = (PackageInternalNameMatcherOperand) operand;
                    addPackageIndex(packageInternalNameMatcherOperand.getPackageInternalName(), indexValue);
                } else {
                    throw new IllegalArgumentException("invalid matcher or execution planner - unknown operand. condition=" + condition + ", unknown operand=" + operand);
                }
            }
        }

        private void addPackageIndex(final String packageInternalName, final IndexValue indexValue) {
            Set<IndexValue> indexValueSet = packageNameBasedIndex.get(packageInternalName);
            if (indexValueSet == null) {
                indexValueSet = new LinkedHashSet<>();
                packageNameBasedIndex.put(packageInternalName, indexValueSet);
            }
            indexValueSet.add(indexValue);
        }

        Map<String, IndexValue> buildClassNameBasedIndex() {
            return classNameBasedIndex;
        }

        /**
         * @return a flat array sorted by package internal name so that the lookup order is deterministic.
         */
        PackageIndex[] buildPackageIndexes() {
            final PackageIndex[] packageIndexes = new PackageIndex[packageNameBasedIndex.size()];
            int i = 0;
            for (Map.Entry<String, Set<IndexValue>> entry : packageNameBasedIndex.entrySet()) {
                final IndexValue[] values = entry.getValue().toArray(new IndexValue[0]);
                packageIndexes[i++] = new PackageIndex(entry.getKey(), values);
            }
            Arrays.sort(packageIndexes, PackageIndex.PACKAGE_NAME_ORDER);
            return packageIndexes;
        }
    }

    static class PackageIndex {
        static final Comparator<PackageIndex> PACKAGE_NAME_ORDER = Comparator.comparing(o -> o.packageInternalName);

        private final String packageInternalName;
        private final IndexValue[] values;

        PackageIndex(final String packageInternalName, final IndexValue[] values) {
            this.packageInternalName = Objects.requireNonNull(packageInternalName, "packageInternalName");
            this.values = Objects.requireNonNull(values, "values");
        }
    }

    static class IndexValue {
        private static final AtomicLongFieldUpdater<IndexValue> ACCUMULATOR_UPDATER
                = AtomicLongFieldUpdater.newUpdater(IndexValue.class, "accumulatorTimeMillis");

        private final MatcherOperand operand;
        private final ClassFileTransformer transformer;
        // updated through ACCUMULATOR_UPDATER: one long per index entry instead of an AtomicLong object
        private volatile long accumulatorTimeMillis;

        public IndexValue(final MatcherOperand operand, final ClassFileTransformer transformer) {
            this.operand = Objects.requireNonNull(operand, "operand");
            this.transformer = Objects.requireNonNull(transformer, "transformer");
        }

        public long accumulatorTime(final long startTimeMillis) {
            final long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
            ACCUMULATOR_UPDATER.addAndGet(this, elapsedTimeMillis);
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
/*
 * Copyright 2026 NAVER Corp.
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

import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collects the class and package based transformers during registration and freezes them into read-only indexes.
 */
class IndexBuilder {
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

    /**
     * @return the read-only index; the class name index is asked first, then the sorted package entries
     */
    TransformerIndex build(final IndexValueMatcher matcher) {
        return new BasedMatcherIndex(this.classNameBasedIndex, buildPackageEntries(), matcher);
    }

    /**
     * @return a flat array sorted by package internal name so that the lookup order is deterministic
     */
    private BasedMatcherIndex.PackageEntry[] buildPackageEntries() {
        final BasedMatcherIndex.PackageEntry[] entries = new BasedMatcherIndex.PackageEntry[packageNameBasedIndex.size()];
        // type token for toArray, reused across the loop; toArray allocates the result array itself.
        final IndexValue[] EMPTY = new IndexValue[0];
        int i = 0;
        for (Map.Entry<String, Set<IndexValue>> entry : packageNameBasedIndex.entrySet()) {
            final IndexValue[] values = entry.getValue().toArray(EMPTY);
            entries[i++] = new BasedMatcherIndex.PackageEntry(entry.getKey(), values);
        }
        Arrays.sort(entries, BasedMatcherIndex.PackageEntry.PACKAGE_NAME_ORDER);
        return entries;
    }
}

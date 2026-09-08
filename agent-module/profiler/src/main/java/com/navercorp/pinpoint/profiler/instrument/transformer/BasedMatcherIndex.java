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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;

import java.lang.instrument.ClassFileTransformer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 * The class name index is asked first by exact class internal name, then the package entries are scanned
 * linearly in package internal name order.
 */
class BasedMatcherIndex implements TransformerIndex {
    private final Map<String, IndexValue> classNameIndex;
    private final PackageEntry[] packageEntries;
    private final IndexValueMatcher matcher;

    BasedMatcherIndex(final Map<String, IndexValue> classNameIndex, final PackageEntry[] packageEntries, final IndexValueMatcher matcher) {
        this.classNameIndex = Objects.requireNonNull(classNameIndex, "classNameIndex");
        this.packageEntries = Objects.requireNonNull(packageEntries, "packageEntries");
        this.matcher = Objects.requireNonNull(matcher, "matcher");
    }

    @Override
    public ClassFileTransformer find(final ClassLoader classLoader, final String classInternalName, final ClassMetadataWrapper classMetadata) {
        final ClassFileTransformer classBased = findClassBased(classLoader, classInternalName, classMetadata);
        if (classBased != null) {
            return classBased;
        }
        return findPackageBased(classLoader, classInternalName, classMetadata);
    }

    private ClassFileTransformer findClassBased(final ClassLoader classLoader, final String classInternalName, final ClassMetadataWrapper classMetadata) {
        final IndexValue indexValue = this.classNameIndex.get(classInternalName);
        if (indexValue == null) {
            return null;
        }
        if (indexValue.getOperand() instanceof ClassInternalNameMatcherOperand) {
            // single operand, the class name itself is the whole condition.
            return indexValue.getTransformer();
        }
        return this.matcher.match(classLoader, indexValue, classMetadata);
    }

    private ClassFileTransformer findPackageBased(final ClassLoader classLoader, final String classInternalName, final ClassMetadataWrapper classMetadata) {
        for (PackageEntry entry : this.packageEntries) {
            if (classInternalName.startsWith(entry.packageInternalName)) {
                for (IndexValue value : entry.values) {
                    final ClassFileTransformer transformer = this.matcher.match(classLoader, value, classMetadata);
                    if (transformer != null) {
                        return transformer;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "BasedMatcherIndex{classNames=" + classNameIndex.size() + ", packageEntries=" + Arrays.toString(packageEntries) + '}';
    }

    /**
     * The index values registered under one package internal name, in registration order.
     */
    static class PackageEntry {
        static final Comparator<PackageEntry> PACKAGE_NAME_ORDER = Comparator.comparing(o -> o.packageInternalName);

        private final String packageInternalName;
        private final IndexValue[] values;

        PackageEntry(final String packageInternalName, final IndexValue[] values) {
            this.packageInternalName = Objects.requireNonNull(packageInternalName, "packageInternalName");
            this.values = Objects.requireNonNull(values, "values");
        }

        @Override
        public String toString() {
            return "PackageEntry{packageInternalName=" + packageInternalName + ", values=" + Arrays.toString(values) + '}';
        }
    }
}

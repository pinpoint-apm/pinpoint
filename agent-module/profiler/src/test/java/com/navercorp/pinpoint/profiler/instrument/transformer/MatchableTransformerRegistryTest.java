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
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.config.DefaultInstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.profiler.plugin.Foo;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.sender.Bar;
import com.navercorp.pinpoint.profiler.util.BytecodeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author jaehong.kim
 */
public class MatchableTransformerRegistryTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void findTransformer() {
        List<MatchableClassFileTransformer> matchableClassFileTransformerList = new ArrayList<>();
        MockMatchableClassFileTransformer mock1 = new MockMatchableClassFileTransformer(Matchers.newPackageBasedMatcher("com.navercorp.pinpoint.profiler.plugin"));
        MockMatchableClassFileTransformer mock2 = new MockMatchableClassFileTransformer(Matchers.newPackageBasedMatcher("com.navercorp.pinpoint.profiler.sender"));


        matchableClassFileTransformerList.add(mock1);
        matchableClassFileTransformerList.add(mock2);

        MatchableTransformerRegistry registry = MatchableTransformerRegistry.newBuilder(new DefaultInstrumentMatcherCacheConfig())
                .addAll(matchableClassFileTransformerList)
                .build();

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = Foo.class;
        byte[] classFileByteCodes = BytecodeUtils.getClassFile(classLoader, clazz.getName());

        ClassFileTransformer classFileTransformer = registry.findTransformer(classLoader, "com/navercorp/pinpoint/profiler/plugin/Foo", classFileByteCodes);


        clazz = Bar.class;
        classFileByteCodes = BytecodeUtils.getClassFile(classLoader, clazz.getName());
        classFileTransformer = registry.findTransformer(classLoader, "com/navercorp/pinpoint/profiler/sender/Bar", classFileByteCodes);
        logger.info("classFileTransformer : {}", classFileTransformer);
    }

    @Test
    public void findTransformer_first_package_in_lexicographic_order_wins() {
        MockMatchableClassFileTransformer broad = new MockMatchableClassFileTransformer(Matchers.newPackageBasedMatcher("com.navercorp.pinpoint.profiler"));
        MockMatchableClassFileTransformer narrow = new MockMatchableClassFileTransformer(Matchers.newPackageBasedMatcher("com.navercorp.pinpoint.profiler.plugin"));
        MockMatchableClassFileTransformer other = new MockMatchableClassFileTransformer(Matchers.newPackageBasedMatcher("com.navercorp.pinpoint.profiler.sender"));

        List<MatchableClassFileTransformer> matchableClassFileTransformerList = new ArrayList<>();
        matchableClassFileTransformerList.add(narrow);
        matchableClassFileTransformerList.add(other);
        matchableClassFileTransformerList.add(broad);

        MatchableTransformerRegistry registry = MatchableTransformerRegistry.newBuilder(new DefaultInstrumentMatcherCacheConfig())
                .addAll(matchableClassFileTransformerList)
                .build();

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        byte[] fooBytes = BytecodeUtils.getClassFile(classLoader, Foo.class.getName());
        assertSame(broad, registry.findTransformer(classLoader, "com/navercorp/pinpoint/profiler/plugin/Foo", fooBytes));

        byte[] barBytes = BytecodeUtils.getClassFile(classLoader, Bar.class.getName());
        assertSame(broad, registry.findTransformer(classLoader, "com/navercorp/pinpoint/profiler/sender/Bar", barBytes));

        assertNull(registry.findTransformer(classLoader, "com/navercorp/pinpoint/bootstrap/Baz", fooBytes));
    }

    @Test
    public void find_scans_packages_in_array_order_and_stops_at_first_match() {
        IndexValue rejected = new IndexValue(new PackageInternalNameMatcherOperand("com.navercorp.a"), new MockMatchableClassFileTransformer(null));
        IndexValue accepted = new IndexValue(new PackageInternalNameMatcherOperand("com.navercorp.a"), new MockMatchableClassFileTransformer(null));
        IndexValue notReached = new IndexValue(new PackageInternalNameMatcherOperand("com.navercorp.a.b"), new MockMatchableClassFileTransformer(null));
        BasedMatcherIndex.PackageEntry[] entries = {
                new BasedMatcherIndex.PackageEntry("com/navercorp/a", new IndexValue[]{rejected, accepted}),
                new BasedMatcherIndex.PackageEntry("com/navercorp/a/b", new IndexValue[]{notReached}),
        };
        RecordingTransformerMatcher transformerMatcher = new RecordingTransformerMatcher(accepted.getOperand());
        TransformerIndex index = new BasedMatcherIndex(Collections.<String, IndexValue>emptyMap(), entries, new IndexValueMatcher(transformerMatcher));

        ClassMetadataWrapper metadata = new ClassMetadataWrapper(null, null);
        assertSame(accepted.getTransformer(), index.find(null, "com/navercorp/a/b/Foo", metadata));
        // both values of the first entry were evaluated, the second entry was never reached.
        assertEquals(Arrays.asList(rejected.getOperand(), accepted.getOperand()), transformerMatcher.evaluated);

        transformerMatcher.evaluated.clear();
        assertNull(index.find(null, "org/other/Bar", metadata));
        assertTrue(transformerMatcher.evaluated.isEmpty());
    }

    @Test
    public void find_skips_the_matcher_for_a_single_class_operand() {
        IndexValue single = new IndexValue(new ClassInternalNameMatcherOperand("com.navercorp.Single"), new MockMatchableClassFileTransformer(null));
        MatcherOperand compound = new ClassInternalNameMatcherOperand("com.navercorp.Compound").and(new InterfaceInternalNameMatcherOperand("java.lang.Runnable", false));
        IndexValue composite = new IndexValue(compound, new MockMatchableClassFileTransformer(null));
        Map<String, IndexValue> map = new HashMap<>();
        map.put("com/navercorp/Single", single);
        map.put("com/navercorp/Compound", composite);
        RecordingTransformerMatcher transformerMatcher = new RecordingTransformerMatcher(null);
        TransformerIndex index = new BasedMatcherIndex(map, new BasedMatcherIndex.PackageEntry[0], new IndexValueMatcher(transformerMatcher));

        ClassMetadataWrapper metadata = new ClassMetadataWrapper(null, null);
        assertSame(single.getTransformer(), index.find(null, "com/navercorp/Single", metadata));
        assertTrue(transformerMatcher.evaluated.isEmpty());

        assertNull(index.find(null, "com/navercorp/Compound", metadata));
        assertEquals(Arrays.asList(compound), transformerMatcher.evaluated);

        assertNull(index.find(null, "com/navercorp/Unknown", metadata));
    }

    @Test
    public void find_asks_the_class_name_index_before_the_packages() {
        IndexValue byClass = new IndexValue(new ClassInternalNameMatcherOperand("com.navercorp.a.Foo"), new MockMatchableClassFileTransformer(null));
        IndexValue byPackage = new IndexValue(new PackageInternalNameMatcherOperand("com.navercorp.a"), new MockMatchableClassFileTransformer(null));
        Map<String, IndexValue> map = new HashMap<>();
        map.put("com/navercorp/a/Foo", byClass);
        BasedMatcherIndex.PackageEntry[] entries = {new BasedMatcherIndex.PackageEntry("com/navercorp/a", new IndexValue[]{byPackage})};
        RecordingTransformerMatcher transformerMatcher = new RecordingTransformerMatcher(byPackage.getOperand());
        TransformerIndex index = new BasedMatcherIndex(map, entries, new IndexValueMatcher(transformerMatcher));

        ClassMetadataWrapper metadata = new ClassMetadataWrapper(null, null);
        assertSame(byClass.getTransformer(), index.find(null, "com/navercorp/a/Foo", metadata));
        assertTrue(transformerMatcher.evaluated.isEmpty());

        assertSame(byPackage.getTransformer(), index.find(null, "com/navercorp/a/Bar", metadata));
        assertEquals(Arrays.asList(byPackage.getOperand()), transformerMatcher.evaluated);
    }

    @Test
    public void accumulatorTime() throws Exception {
        IndexValue value = new IndexValue(new PackageInternalNameMatcherOperand("com.navercorp"), new MockMatchableClassFileTransformer(null));
        long startTime = System.currentTimeMillis();
        Thread.sleep(10);
        long elapsed = value.accumulatorTime(startTime);
        assertTrue(elapsed >= 10);
        assertEquals(elapsed, value.getAccumulatorTimeMillis());
    }

    /**
     * accepts only the given operand and records every operand it was asked about, in order.
     */
    private static class RecordingTransformerMatcher implements TransformerMatcher {
        private final MatcherOperand accept;
        final List<MatcherOperand> evaluated = new ArrayList<>();

        RecordingTransformerMatcher(MatcherOperand accept) {
            this.accept = accept;
        }

        @Override
        public boolean match(ClassLoader classLoader, MatcherOperand operand, InternalClassMetadata classMetadata) {
            evaluated.add(operand);
            return operand == accept;
        }
    }

    private static class MockMatchableClassFileTransformer implements MatchableClassFileTransformer {
        public Matcher matcher;

        public MockMatchableClassFileTransformer(Matcher matcher) {
            this.matcher = matcher;
        }

        @Override
        public Matcher getMatcher() {
            return this.matcher;
        }

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return new byte[0];
        }

        public String toString() {
            return String.valueOf(matcher);
        }
    }
}
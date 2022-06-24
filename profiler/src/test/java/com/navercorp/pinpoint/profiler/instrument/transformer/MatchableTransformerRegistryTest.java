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
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.profiler.instrument.config.DefaultInstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.profiler.plugin.Foo;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.sender.Bar;
import com.navercorp.pinpoint.profiler.util.BytecodeUtils;
import org.junit.jupiter.api.Test;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class MatchableTransformerRegistryTest {

    @Test
    public void findTransformer() {
        List<MatchableClassFileTransformer> matchableClassFileTransformerList = new ArrayList<>();
        MockMatchableClassFileTransformer mock1 = new MockMatchableClassFileTransformer(Matchers.newPackageBasedMatcher("com.navercorp.pinpoint.profiler.plugin"));
        MockMatchableClassFileTransformer mock2 = new MockMatchableClassFileTransformer(Matchers.newPackageBasedMatcher("com.navercorp.pinpoint.profiler.sender"));


        matchableClassFileTransformerList.add(mock1);
        matchableClassFileTransformerList.add(mock2);

        MatchableTransformerRegistry registry = new MatchableTransformerRegistry(new DefaultInstrumentMatcherCacheConfig(), matchableClassFileTransformerList);

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = Foo.class;
        byte[] classFileByteCodes = BytecodeUtils.getClassFile(classLoader, clazz.getName());

        ClassFileTransformer classFileTransformer = registry.findTransformer(classLoader, "com/navercorp/pinpoint/profiler/plugin/Foo", classFileByteCodes);


        clazz = Bar.class;
        classFileByteCodes = BytecodeUtils.getClassFile(classLoader, clazz.getName());
        classFileTransformer = registry.findTransformer(classLoader, "com/navercorp/pinpoint/profiler/sender/Bar", classFileByteCodes);
        System.out.println(classFileTransformer.toString());
    }

    @Test
    public void packageNameBasedIndex() {
        // sorted
        TreeMap<String, String> packageNameBasedIndex = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String key1, String key2) {
                return key1.compareTo(key2);
            }
        });

        packageNameBasedIndex.put("a", "a");
        packageNameBasedIndex.put("aa", "a");
        packageNameBasedIndex.put("bbbb", "a");
        packageNameBasedIndex.put("bb", "a");
        packageNameBasedIndex.put("bbb", "a");
        packageNameBasedIndex.put("c", "a");
        packageNameBasedIndex.put("ccccc", "a");
        packageNameBasedIndex.put("cccc", "a");
        packageNameBasedIndex.put("dddddddddddd", "a");

        String[] keys = packageNameBasedIndex.keySet().toArray(new String[9]);
        assertEquals("a", keys[0]);
        assertEquals("aa", keys[1]);
        assertEquals("bb", keys[2]);
        assertEquals("bbb", keys[3]);
        assertEquals("bbbb", keys[4]);
        assertEquals("c", keys[5]);
        assertEquals("cccc", keys[6]);
        assertEquals("ccccc", keys[7]);
        assertEquals("dddddddddddd", keys[8]);
    }

    @Test
    public void accumulatorTime() throws Exception {
        IndexValue value = new IndexValue(null, null);
        long startTime = System.currentTimeMillis();
        Thread.sleep(10);
        value.accumulatorTime(startTime);
    }

    static class IndexValue {
        final MatcherOperand operand;
        final ClassFileTransformer transformer;
        final AtomicLong accumulatorTimeMillis = new AtomicLong(0);

        public IndexValue(final MatcherOperand operand, final ClassFileTransformer transformer) {
            this.operand = operand;
            this.transformer = transformer;
        }

        public long accumulatorTime(final long startTimeMillis) {
            final long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
            return accumulatorTimeMillis.addAndGet(elapsedTimeMillis);
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
            return matcher.toString();
        }
    }
}
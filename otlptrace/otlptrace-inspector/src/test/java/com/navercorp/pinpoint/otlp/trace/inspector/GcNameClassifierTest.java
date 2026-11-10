/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.inspector;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GcNameClassifierTest {

    private final GcNameClassifier classifier = new GcNameClassifier();

    @Test
    void sharedHeapPools_areTheGenerationalZgcOnes() {
        assertThat(classifier.isSharedHeapPool("ZGC Young Generation")).isTrue();
        assertThat(classifier.isSharedHeapPool("ZGC Old Generation")).isTrue();
        assertThat(classifier.isSharedHeapPool("ZHeap")).isFalse();
        assertThat(classifier.isSharedHeapPool("G1 Old Gen")).isFalse();
        assertThat(classifier.isSharedHeapPool("PS Eden Space")).isFalse();
    }

    @Test
    void heapPools_nameTheirCollector() {
        assertThat(classifier.heapPoolGcType("G1 Old Gen")).isEqualTo(JvmGcType.G1);
        assertThat(classifier.heapPoolGcType("PS Eden Space")).isEqualTo(JvmGcType.PARALLEL);
        assertThat(classifier.heapPoolGcType("Tenured Gen")).isEqualTo(JvmGcType.SERIAL);
        assertThat(classifier.heapPoolGcType("CMS Old Gen")).isEqualTo(JvmGcType.CMS);
        assertThat(classifier.heapPoolGcType("ZHeap")).isEqualTo(JvmGcType.ZGC);
        assertThat(classifier.heapPoolGcType("ZGC Old Generation")).isEqualTo(JvmGcType.ZGC);
        assertThat(classifier.heapPoolGcType("Shenandoah")).isEqualTo(JvmGcType.UNKNOWN);
        assertThat(classifier.heapPoolGcType("Metaspace")).isEqualTo(JvmGcType.UNKNOWN);
    }

    @ParameterizedTest
    @CsvSource({
            "MarkSweepCompact, SERIAL, true",
            "Copy, SERIAL, false",
            "PS MarkSweep, PARALLEL, true",
            "PS Scavenge, PARALLEL, false",
            "ConcurrentMarkSweep, CMS, true",
            "ParNew, CMS, false",
            "G1 Old Generation, G1, true",
            "G1 Young Generation, G1, false",
            "G1 Concurrent GC, G1, false",
            "ZGC Major Pauses, ZGC, true",
            "ZGC Minor Pauses, ZGC, false",
            "ZGC Major Cycles, ZGC, false",
            "ZGC Minor Cycles, ZGC, false",
            "ZGC Pauses, ZGC, true",
            "ZGC Cycles, ZGC, false",
            "ZGC, ZGC, true",
    })
    void knownCollectors(String name, JvmGcType type, boolean old) {
        final GcClassification classification = classifier.classify(name);
        assertThat(classification.gcType()).isEqualTo(type);
        assertThat(classification.old()).isEqualTo(old);
    }

    @ParameterizedTest
    @CsvSource({
            "Shenandoah Pauses, false",
            "Shenandoah Cycles, false",
            "Epsilon Heap, false",
            "Vendor Old Collector, true",
            "Vendor Full GC, true",
            "Vendor Major Collection, true",
            "Vendor Concurrent Cycles, false",
    })
    void unknownCollectors_goBySide(String name, boolean old) {
        final GcClassification classification = classifier.classify(name);
        assertThat(classification.gcType()).isEqualTo(JvmGcType.UNKNOWN);
        assertThat(classification.old()).isEqualTo(old);
    }
}

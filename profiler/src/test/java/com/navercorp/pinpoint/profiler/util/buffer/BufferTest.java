/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.util.buffer;

import com.navercorp.pinpoint.profiler.util.queue.ArrayBuffer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class BufferTest {

    @Test
    void put() {
        ArrayBuffer<String> buffer = new ArrayBuffer<>(3);
        buffer.put("a");
        buffer.put("b");
        buffer.put("c");
        buffer.put("1");
        buffer.put("2");
    }

    @Test
    void putList() {
        ArrayBuffer<String> buffer = new ArrayBuffer<>(3);
        buffer.put("a");
        buffer.put("b");
        buffer.put("c");

        buffer.put(Arrays.asList("1", "2", "3", "4"));

        Assertions.assertThat(buffer.size()).isEqualTo(7);
    }

    @Test
    void putList2() {
        ArrayBuffer<String> buffer = new ArrayBuffer<>(1);
        buffer.put(Arrays.asList("1", "2", "3", "4"));

        Assertions.assertThat(buffer.size()).isEqualTo(4);
    }


    @Test
    void drain() {
        ArrayBuffer<String> buffer = new ArrayBuffer<>(3);
        buffer.put("a");
        buffer.put("b");
        buffer.put("c");

        List<String> drain = buffer.drain();
        Assertions.assertThat(drain)
                .hasSize(3)
                .containsExactly("a", "b", "c");

        Assertions.assertThat(buffer.size()).isEqualTo(0);
    }

    @Test
    void drain_empty() {
        ArrayBuffer<String> buffer = new ArrayBuffer<>(3);

        List<String> drain = buffer.drain();
        Assertions.assertThat(drain)
               .isEmpty();
    }


    @Test
    void overflow() {
        ArrayBuffer<String> buffer = new ArrayBuffer<>(3);
        buffer.put("a");
        Assertions.assertThat(buffer.isOverflow()).isFalse();
        buffer.put("b");
        buffer.put("c");
        Assertions.assertThat(buffer.isOverflow()).isTrue();

        buffer.put("1");
        Assertions.assertThat(buffer.isOverflow()).isTrue();
    }
}
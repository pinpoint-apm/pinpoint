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

import com.navercorp.pinpoint.profiler.util.queue.ArrayViewList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class ArrayViewListTest {

    String[] array = new String[] {"a", "b", "c"};

    @Test
    void size() {
        List<String> list1 = new ArrayViewList<>(array, 2);
        Assertions.assertThat(list1).hasSize(2);

        List<String> list2 = new ArrayViewList<>(array, 3);
        Assertions.assertThat(list2).hasSize(3);
    }

    @Test
    void size_overflow() {
        Assertions.assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class)
                        .isThrownBy(() -> new ArrayViewList<>(array, 4));
    }


    @Test
    void toArray() {
        List<String> list = new ArrayViewList<>(array, 2);

        Object[] copy = list.toArray();
        Assertions.assertThat(copy).hasSize(2);
    }

    @Test
    void testToArray() {
        List<String> list = new ArrayViewList<>(array, 2);

        Object[] copy2 = list.toArray(new Object[0]);

        Assertions.assertThat(copy2).hasSize(2);
    }

    @Test
    void get() {
        List<String> list = new ArrayViewList<>(array, 2);

        String s1 = list.get(1);
        Assertions.assertThat(s1).isSameAs("b");

        Assertions.assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class)
                .isThrownBy(() -> list.get(3));
    }

    @Test
    void set() {
        String[] array = new String[] {"a", "b", "c"};
        List<String> list = new ArrayViewList<>(array, 2);

        list.set(0, "1");

        Assertions.assertThat(list.get(0)).isSameAs("1");


        Assertions.assertThatExceptionOfType(ArrayIndexOutOfBoundsException.class)
                .isThrownBy(() -> list.set(3, "3"));
    }

    @Test
    void indexOf() {
        List<String> list = new ArrayViewList<>(array, 2);

        Assertions.assertThat(list.indexOf("b")).isEqualTo(1);
        Assertions.assertThat(list.indexOf("c")).isEqualTo(-1);

        Assertions.assertThat(list.indexOf(null)).isEqualTo(-1);
    }

    @Test
    void iterator() {
        List<String> list = new ArrayViewList<>(array, 2);

        Assertions.assertThat(list.iterator()).toIterable()
                .containsExactly("a", "b");
    }

    @Test
    void contains() {
        List<String> list = new ArrayViewList<>(array, 2);

        Assertions.assertThat(list.contains("b")).isTrue();
        Assertions.assertThat(list.contains("c")).isFalse();
    }

    @Test
    void sort() {
        String[] array = new String[] {"c", "b", "a", "1"};
        List<String> list = new ArrayViewList<>(array, 3);
        list.sort(String::compareTo);
        Assertions.assertThat(list).containsExactly("a", "b", "c");

    }
}
/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase.limit;

import com.navercorp.pinpoint.common.hbase.ResultSizeHandlers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

class ResultSizeHandlersTest {

    @Test
    void getHandler_array() {
        int[] array = new int[10];

        ToIntFunction<int[]> handler = ResultSizeHandlers.getHandler(array);
        Assertions.assertEquals(ResultSizeHandlers.ARRAY_HANDLER, handler);

        int size = handler.applyAsInt(array);
        Assertions.assertEquals(10, size);
    }

    @Test
    void getHandler_map() {
        Map<String, String> map = Map.of("key1", "value1", "key2", "value2");

        ToIntFunction<Map<?, ?>> handler = ResultSizeHandlers.getHandler(map);
        Assertions.assertEquals(ResultSizeHandlers.MAP_HANDLER, handler);

        int size = handler.applyAsInt(map);
        Assertions.assertEquals(2, size);
    }

    @Test
    void getHandler_collection() {
        List<String> list = List.of("key1", "key2", "key3");

        ToIntFunction<List<?>> handler = ResultSizeHandlers.getHandler(list);
        Assertions.assertEquals(ResultSizeHandlers.COLLECTION_HANDLER, handler);

        int size = handler.applyAsInt(list);
        Assertions.assertEquals(3, size);
    }

    @Test
    void getHandler_class() {
        List<String> list = List.of("key1", "key2", "key3");

        ToIntFunction<List<?>> handler = ResultSizeHandlers.getHandler(list.getClass());
        Assertions.assertEquals(ResultSizeHandlers.COLLECTION_HANDLER, handler);

        int size = handler.applyAsInt(list);
        Assertions.assertEquals(3, size);
    }
}
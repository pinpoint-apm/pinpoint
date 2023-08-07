/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.profiler.sql;

import java.util.List;

/**
 * @author emeroad
 */
public interface SqlNormalizer {

    NormalizedSql normalizeSql(String sql);

    default String combineOutputParams(String sql, List<String> outputParams) {
        return this.combineOutputParams(sql, outputParams::get);
    }

    String combineOutputParams(String sql, IndexedSupplier<String> outputParams);

    String combineBindValues(String sql, List<String> bindValues);

    interface IndexedSupplier<T> {
        T get(int index) throws IndexOutOfBoundsException;
    }
}

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

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * @author emeroad
 */
public class LastRowResultsExtractor<T> implements ResultsExtractor<List<T>> {

    private int limit = Integer.MAX_VALUE;
    private final RowMapper<T> rowMapper;

    @Nullable
    private final LastRowHandler<T> rowHandler;
    private final ToIntFunction<T> resultSizeHandler;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }


    /**
     * Create a new RowMapperResultSetExtractor.
     *
     * @param rowMapper the RowMapper which creates an object for each row
     */
    public LastRowResultsExtractor(RowMapper<T> rowMapper, int limit, LastRowHandler<T> rowHandler) {
        this.rowMapper = Objects.requireNonNull(rowMapper, "RowMapper");
        this.limit = limit;
        this.rowHandler = rowHandler;
        this.resultSizeHandler = resolveResultSizeHandler(rowMapper);
    }

    private ToIntFunction<T> resolveResultSizeHandler(RowMapper<T> rowMapper) {
        if (rowMapper instanceof RowTypeHint hint) {
            Class<?> clazz = hint.rowType();
            return ResultSizeHandlers.getHandler(clazz);
        }
        return new LazyResultSizeHandler<>();
    }

    public List<T> extractData(ResultScanner results) throws Exception {
        final List<T> rs = new ArrayList<>();
        int rowNum = 0;
        T t = null;

        for (Result result : results) {
            t = this.rowMapper.mapRow(result, rowNum);
            if (t == null) {
                // empty
            } else {
                rowNum += resultSizeHandler.applyAsInt(t);
            }
            rs.add(t);
            if (rowNum >= limit) {
                break;
            }
        }
        if (rowHandler != null) {
            rowHandler.handleLastRow(t);
        }
        return rs;
    }
}

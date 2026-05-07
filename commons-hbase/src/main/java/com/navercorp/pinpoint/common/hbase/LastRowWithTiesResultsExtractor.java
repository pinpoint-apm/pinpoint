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

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Variant of {@link LastRowResultsExtractor} with "limit with ties" semantics:
 * after the row limit is reached, keep accepting subsequent rows whose key
 * (extracted from the raw {@link Result}, e.g. inverted timestamp) equals the
 * boundary row's key. Prevents pagination from dropping rows that share
 * the boundary key.
 */
public class LastRowWithTiesResultsExtractor<T> implements ResultsExtractor<List<T>> {

    private final int limit;
    private final RowMapper<T> rowMapper;
    private final int tieOffset;
    private final int tieLength;

    @Nullable
    private final LastRowHandler<T> rowHandler;
    private final ToIntFunction<T> resultSizeHandler;

    public LastRowWithTiesResultsExtractor(RowMapper<T> rowMapper,
                                           int limit,
                                           int tieOffset,
                                           int tieLength,
                                           @Nullable LastRowHandler<T> rowHandler) {
        this.rowMapper = Objects.requireNonNull(rowMapper, "RowMapper");
        this.limit = limit;
        if (tieOffset < 0) {
            throw new IllegalArgumentException("negative tieOffset:" + tieOffset);
        }
        this.tieOffset = tieOffset;
        if (tieLength <= 0) {
            throw new IllegalArgumentException("non-positive tieLength:" + tieLength);
        }
        this.tieLength = tieLength;
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

    @Override
    public List<T> extractData(ResultScanner results) throws Exception {
        final List<T> rs = new ArrayList<>();
        int rowNum = 0;
        T t = null;
        byte[] boundaryRow = null;

        final Iterator<Result> iterator = results.iterator();

        while (iterator.hasNext()) {
            Result result = iterator.next();
            t = this.rowMapper.mapRow(result, rowNum);
            if (t == null) {
                // empty
            } else {
                rowNum += resultSizeHandler.applyAsInt(t);
            }
            rs.add(t);
            if (rowNum >= limit) {
                boundaryRow = result.getRow();
                break;
            }
        }

        while (iterator.hasNext()) {
            Result result = iterator.next();
            byte[] row = result.getRow();
            if (Arrays.compare(boundaryRow, tieOffset, tieOffset + tieLength,
                    row, tieOffset, tieOffset + tieLength) != 0) {
                break;
            }
            t = this.rowMapper.mapRow(result, rowNum);
            if (t != null) {
                rowNum += resultSizeHandler.applyAsInt(t);
            }
            rs.add(t);
        }

        if (rowHandler != null) {
            rowHandler.handleLastRow(t);
        }
        return rs;
    }
}
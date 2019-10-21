/*
 * Copyright 2014 NAVER Corp.
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class LimitRowMapperResultsExtractor<T> implements ResultsExtractor<List<T>> {

    private static final LimitEventHandler EMPTY = new EmptyLimitEventHandler();

    private int limit = Integer.MAX_VALUE;
    private final RowMapper<T> rowMapper;
    private LimitEventHandler eventHandler;

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
    public LimitRowMapperResultsExtractor(RowMapper<T> rowMapper, int limit) {
        this(rowMapper, limit, EMPTY);
    }

    /**
     * Create a new RowMapperResultSetExtractor.
     *
     * @param rowMapper the RowMapper which creates an object for each row
     */
    public LimitRowMapperResultsExtractor(RowMapper<T> rowMapper, int limit, LimitEventHandler eventHandler) {
        this.rowMapper = Objects.requireNonNull(rowMapper, "RowMapper");
        this.limit = limit;
        this.eventHandler = Objects.requireNonNull(eventHandler, "LimitEventHandler");
    }

    public List<T> extractData(ResultScanner results) throws Exception {
        final List<T> rs = new ArrayList<>();
        int rowNum = 0;
        Result lastResult = null;

        for (Result result : results) {
            final T t = this.rowMapper.mapRow(result, rowNum);
            lastResult = result;
            if (t instanceof Collection) {
                rowNum += ((Collection<?>) t).size();
            } else if (t instanceof Map) {
                rowNum += ((Map<?, ?>) t).size();
            } else if (t == null) {
                // empty
            } else if (t.getClass().isArray()) {
                rowNum += Array.getLength(t);
            } else {
                rowNum++;
            }
            rs.add(t);
            if (rowNum >= limit) {
                break;
            }
        }

        eventHandler.handleLastResult(lastResult);
        return rs;
    }
}

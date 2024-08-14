/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.google.common.collect.Iterables;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class SingleResultsExtractor<T> implements ResultsExtractor<T> {

    private final RowMapper<T> mapper;

    public SingleResultsExtractor(RowMapper<T> mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public RowMapper<T> getMapper() {
        return mapper;
    }

    @Override
    public T extractData(ResultScanner results) throws Exception {
        final Result first = Iterables.getFirst(results, null);
        if (first == null) {
            return null;
        }
        return mapper.mapRow(first, 0);

    }
}

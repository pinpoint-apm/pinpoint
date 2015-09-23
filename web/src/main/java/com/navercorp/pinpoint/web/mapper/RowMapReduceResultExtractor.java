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

package com.navercorp.pinpoint.web.mapper;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.util.Assert;

import com.navercorp.pinpoint.common.hbase.RowReducer;

/**
 * @author jaehong.kim
 */
public class RowMapReduceResultExtractor<T> implements ResultsExtractor<T> {
    private final RowMapper<T> rowMapper;
    private final RowReducer<T> rowReducer;

    public RowMapReduceResultExtractor(RowMapper<T> rowMapper, RowReducer<T> rowReducer) {
        Assert.notNull(rowMapper, "RowMapper is required");
        this.rowMapper = rowMapper;
        this.rowReducer = rowReducer;
    }

    @Override
    public T extractData(ResultScanner results) throws Exception {
        int rowNum = 0;
        T r = null;
        for (Result result : results) {
            T map = this.rowMapper.mapRow(result, rowNum++);
            r = rowReducer.reduce(map);
        }

        return r;
    }
}

/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListMergeResultsExtractor<T> implements ResultsExtractor<List<T>> {

    private final RowMapper<List<T>> mapper;

    public ListMergeResultsExtractor(RowMapper<List<T>> mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public List<T> extractData(ResultScanner results) throws Exception {
        List<T> agentEvents = new ArrayList<>();
        int rowNum = 0;
        for (Result result : results) {
            List<T> lists = mapper.mapRow(result, rowNum++);
            if (!lists.isEmpty()) {
                agentEvents.addAll(lists);
            }
        }
        return agentEvents;
    }

    public RowMapper<List<T>> getMapper() {
        return mapper;
    }

    @Override
    public String toString() {
        return "ListMergeResultsExtractor{" +
                "mapper=" + mapper +
                '}';
    }
}
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

package com.navercorp.pinpoint.common.hbase.rowmapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RequestAwareRowMapperAdaptor<T, ReqT> implements RowMapper<T> {

    private final List<ReqT> requestList;
    private final RequestAwareRowMapper<T, ReqT> requestAwareRowMapper;

    public RequestAwareRowMapperAdaptor(List<ReqT> requestList, RequestAwareRowMapper<T, ReqT> requestAwareRowMapper) {
        this.requestList = Objects.requireNonNull(requestList, "requestList");
        this.requestAwareRowMapper = Objects.requireNonNull(requestAwareRowMapper, "requestAwareRowMapper");
    }

    @Override
    public T mapRow(Result result, int rowNum) throws Exception {
        final ReqT request = requestList.get(rowNum);
        return this.requestAwareRowMapper.mapRow(result, rowNum, request);
    }
}

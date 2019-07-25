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

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RequestAwareDynamicRowMapper<T, ReqT> implements RequestAwareRowMapper<T, ReqT> {
    private final Function<ReqT, RowMapper<T>> rowMapperFunction;

    public RequestAwareDynamicRowMapper(Function<ReqT, RowMapper<T>> rowMapperFunction) {
        this.rowMapperFunction = Objects.requireNonNull(rowMapperFunction, "rowMapperFunction");
    }

    @Override
    public T mapRow(Result result, int rowNum, ReqT request) throws Exception {
        final RowMapper<T> spanMapper = rowMapperFunction.apply(request);
        final T mapRow = spanMapper.mapRow(result, rowNum);
        return mapRow;
    }
};

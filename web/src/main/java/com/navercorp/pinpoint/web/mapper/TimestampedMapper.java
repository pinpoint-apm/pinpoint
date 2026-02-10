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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;

import java.util.Objects;

public class TimestampedMapper<T> implements RowMapper<Timestamped<T>> {

    private final RowMapper<T> delegateMapper;

    public TimestampedMapper(RowMapper<T> delegateMapper) {
        this.delegateMapper = Objects.requireNonNull(delegateMapper, "delegateMapper");
    }

    @Override
    public Timestamped<T> mapRow(Result result, int rowNum) throws Exception {
        T t = delegateMapper.mapRow(result, rowNum);
        if (t == null) {
            return null;
        }

        long timestamp = result.rawCells()[0].getTimestamp();
        return new Timestamped<>(t, timestamp);
    }
}

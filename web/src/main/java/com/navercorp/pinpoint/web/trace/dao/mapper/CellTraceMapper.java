/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.dao.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.web.util.CellTracker;
import com.navercorp.pinpoint.web.util.DefaultCellTracker;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CellTraceMapper<T> implements RowMapper<T> {
    private final RowMapper<T> delegate;

    public static <T> RowMapper<T> wrap(RowMapper<T> deleagate) {

        return new CellTraceMapper<T>(deleagate);
    }


    private CellTraceMapper(RowMapper<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public T mapRow(Result result, int rowNum) throws Exception {
        final T returnValue = this.delegate.mapRow(result, rowNum);

        if (!result.isEmpty()) {

            final Cell[] rawCells = result.rawCells();

            final CellTracker cellTracker = new DefaultCellTracker(delegate.getClass().getSimpleName());
            for (Cell cell : rawCells) {
                cellTracker.trace(cell);
            }
            cellTracker.log();
        }

        return returnValue;
    }

}

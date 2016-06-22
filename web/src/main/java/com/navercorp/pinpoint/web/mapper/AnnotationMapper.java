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

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;

import com.navercorp.pinpoint.common.server.bo.AnnotationBoDecoder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class AnnotationMapper implements RowMapper<Map<Long, List<AnnotationBo>>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AnnotationBoDecoder annotationDecoder = new AnnotationBoDecoder();

    @Override
    public Map<Long, List<AnnotationBo>> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
        final Cell[] rawCells = result.rawCells();
        Map<Long, List<AnnotationBo>> annotationList = new HashMap<>();

        for (Cell cell : rawCells) {

            final Buffer buffer = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            long spanId = buffer.readLong();

            if (CellUtil.matchingFamily(cell, HBaseTables.TRACES_CF_ANNOTATION)) {
                final int valueLength = cell.getValueLength();
                if (valueLength == 0) {
                    continue;
                }

                buffer.setOffset(cell.getValueOffset());

                List<AnnotationBo> annotationBoList = annotationDecoder.decode(buffer);
                if (CollectionUtils.isNotEmpty(annotationBoList)) {
                    annotationList.put(spanId, annotationBoList);
                }
            }
        }
        return annotationList;
    }
}

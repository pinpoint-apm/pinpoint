/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanHeader;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.web.trace.dao.mapper.SpanMapperFactory;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HbaseTraceDaoV2Test {

    @SuppressWarnings("unchecked")
    private final HbaseTraceDaoV2 dao = new HbaseTraceDaoV2(
            mock(HbaseOperations.class),
            mock(TableNameProvider.class),
            mock(RowKeyEncoder.class),
            mock(SpanMapperFactory.class));

    @Test
    void createSpanQualifierFilter_keepsEverySpanVariantAndNoChunk() {
        Filter filter = dao.createSpanQualifierFilter();
        assertThat(filter).isInstanceOf(FilterList.class);

        Set<Byte> matchedCodes = new HashSet<>();
        for (Filter f : ((FilterList) filter).getFilters()) {
            byte[] value = ((QualifierFilter) f).getComparator().getValue();
            assertThat(value).as("type prefix is a single byte").hasSize(1);
            matchedCodes.add(value[0]);
        }

        Set<Byte> expected = new HashSet<>();
        for (SpanHeader header : SpanHeader.values()) {
            if (!header.isSpanChunk()) {
                expected.add(header.getCode());
            }
        }

        // all four span variants (SPAN, OTEL_SPAN, SPAN_UID, OTEL_SPAN_UID), no chunk codes
        assertThat(matchedCodes).isEqualTo(expected);
        assertThat(matchedCodes).contains(SpanHeader.SPAN.getCode(), SpanHeader.SPAN_UID.getCode());
        assertThat(matchedCodes).doesNotContain(
                SpanHeader.SPAN_CHUNK.getCode(), SpanHeader.OTEL_SPAN_CHUNK_UID.getCode());
    }
}

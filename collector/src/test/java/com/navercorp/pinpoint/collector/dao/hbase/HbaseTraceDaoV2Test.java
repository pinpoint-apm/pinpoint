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

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.util.DurabilityApplier;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.async.HbasePutWriter;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanChunkSerializerV2;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncoderV0;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanHeaderFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanSerializerV2;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HbaseTraceDaoV2Test {

    @Mock
    private HbasePutWriter putWriter;
    @Mock
    private TableNameProvider tableNameProvider;
    @Mock
    private RowKeyEncoder<ServerTraceId> rowKeyEncoder;

    @Test
    void insertSpanChunkAppliesDurability() {
        SpanEncoderV0 spanEncoder = new SpanEncoderV0(new SpanHeaderFactory(false));
        HbaseTraceDaoV2 dao = new HbaseTraceDaoV2(putWriter, tableNameProvider, rowKeyEncoder,
                new SpanSerializerV2(spanEncoder), new SpanChunkSerializerV2(spanEncoder),
                new DurabilityApplier("SKIP_WAL"));

        SpanChunkBo spanChunk = new SpanChunkBo();
        spanChunk.getSpanOwner().setAgentId("agent");
        spanChunk.getSpanOwner().setApplicationName("application");
        spanChunk.setTransactionId(ServerTraceId.of("agent^100^1"));
        spanChunk.setCollectorAcceptTime(200L);
        spanChunk.addSpanEvent(new SpanEventBo());

        TableName tableName = TableName.valueOf("TraceV2");
        when(tableNameProvider.getTableName(HbaseTables.TRACE_V2_SPAN.getTable())).thenReturn(tableName);
        when(rowKeyEncoder.encodeRowKey(spanChunk.getTransactionId())).thenReturn(new byte[]{1});

        dao.insertSpanChunk(spanChunk);

        ArgumentCaptor<Put> putCaptor = ArgumentCaptor.forClass(Put.class);
        verify(putWriter).put(eq(tableName), putCaptor.capture());
        assertEquals(Durability.SKIP_WAL, putCaptor.getValue().getDurability());
    }
}

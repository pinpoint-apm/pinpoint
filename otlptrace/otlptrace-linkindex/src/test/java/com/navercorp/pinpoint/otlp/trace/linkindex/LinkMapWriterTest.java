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

package com.navercorp.pinpoint.otlp.trace.linkindex;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableCallback;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.OneByteSimpleHash;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidAppRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.UidLinkRowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v3.ColumnNameV3;
import com.navercorp.pinpoint.common.timeseries.window.DefaultTimeSlot;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LinkMapWriterTest {

    private static final TableName IN_TABLE = TableName.valueOf("MapAppIn");
    private static final TableName HOST_TABLE = TableName.valueOf("MapAppHost");

    private final ByteHasher hasher = new OneByteSimpleHash(32);
    private final HbaseOperations hbaseOperations = mock(HbaseOperations.class);
    private final TableNameProvider tableNameProvider = mock(TableNameProvider.class);

    private final Vertex callee = Vertex.of(3, "p.cart", ServiceType.OPENTELEMETRY_SERVER);
    private final Vertex caller = Vertex.of(1, "p.checkout", ServiceType.OPENTELEMETRY_SERVER);
    private final long slot = LinkIndexKeys.slotOf(1_791_958_740_000L);

    @BeforeEach
    void setUp() {
        when(tableNameProvider.getTableName(HbaseTables.MAP_APP_IN.getTable())).thenReturn(IN_TABLE);
        when(tableNameProvider.getTableName(HbaseTables.MAP_APP_HOST.getTable())).thenReturn(HOST_TABLE);
    }

    private LinkStat stat() {
        LinkStat stat = new LinkStat(callee.serviceType().getHistogramSchema());
        stat.add(10, false, 1);
        stat.add(10, false, 1);
        stat.add(4000, true, 1);
        return stat;
    }

    /**
     * The writer puts through {@link HbaseOperations#execute} (synchronous Table.put): run the captured
     * callback against a mock table and return the puts it received.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Put> captured(TableName tableName) {
        ArgumentCaptor<TableCallback> callback = ArgumentCaptor.forClass(TableCallback.class);
        verify(hbaseOperations).execute(eq(tableName), callback.capture());
        Table table = mock(Table.class);
        try {
            callback.getValue().doInTable(table);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
        ArgumentCaptor<List<Put>> puts = ArgumentCaptor.forClass(List.class);
        try {
            verify(table).put(puts.capture());
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
        return puts.getValue();
    }

    @Test
    void inLinkRowAndAbsoluteColumns() {
        LinkMapWriter writer = new LinkMapWriter(hbaseOperations, tableNameProvider, hasher, new DefaultTimeSlot(), true, true);
        Map<LinkKey, LinkStat> stats = new HashMap<>();
        stats.put(new LinkKey(callee, caller, "cart:7070", slot, LinkSource.SAME_SLOT), stat());

        assertThat(writer.write(stats)).isEqualTo(2);

        List<Put> inPuts = captured(IN_TABLE);
        assertThat(inPuts).hasSize(1);
        Put put = inPuts.get(0);

        UidLinkRowKey rowKey = UidLinkRowKey.read(hasher.getSaltKey().size(), put.getRow());
        assertThat(rowKey.getServiceUid()).isEqualTo(3);
        assertThat(rowKey.getApplicationName()).isEqualTo("p.cart");
        assertThat(rowKey.getServiceType()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(rowKey.getTimestamp()).isEqualTo(LinkIndexKeys.slotStartMillis(slot));
        assertThat(rowKey.getLinkServiceUid()).isEqualTo(1);
        assertThat(rowKey.getLinkApplicationName()).isEqualTo("p.checkout");
        assertThat(rowKey.getSubLink()).isEqualTo("_otel");

        HistogramSchema schema = ServiceType.OPENTELEMETRY_SERVER.getHistogramSchema();
        Map<String, Long> columns = columns(put);
        assertThat(columns)
                .containsEntry(key(ColumnNameV3.histogram(schema.findHistogramSlot(10, false).getSlotCode()).getColumnName()), 2L)
                .containsEntry(key(ColumnNameV3.histogram(schema.findHistogramSlot(4000, true).getSlotCode()).getColumnName()), 1L)
                .containsEntry(key(ColumnNameV3.histogram(schema.getSumStatSlot().getSlotCode()).getColumnName()), 4020L)
                .containsEntry(key(ColumnNameV3.histogram(schema.getMaxStatSlot().getSlotCode()).getColumnName()), 4000L)
                .hasSize(4);
    }

    @Test
    void sumAndMaxFollowTheCollectorFlags() {
        LinkMapWriter writer = new LinkMapWriter(hbaseOperations, tableNameProvider, hasher, new DefaultTimeSlot(), false, false);
        writer.write(Map.of(new LinkKey(callee, caller, "cart:7070", slot, LinkSource.CROSS_SLOT), stat()));

        Put put = captured(IN_TABLE).get(0);
        assertThat(columns(put)).hasSize(2);
        assertThat(UidLinkRowKey.read(hasher.getSaltKey().size(), put.getRow()).getSubLink()).isEqualTo("_otelx");
    }

    @Test
    void oneHostPutPerCallerCalleeDestinationAndMinute() {
        LinkMapWriter writer = new LinkMapWriter(hbaseOperations, tableNameProvider, hasher, new DefaultTimeSlot(), true, true);
        Map<LinkKey, LinkStat> stats = new HashMap<>();
        stats.put(new LinkKey(callee, caller, "cart:7070", slot, LinkSource.SAME_SLOT), stat());
        stats.put(new LinkKey(callee, caller, "cart:7070", slot, LinkSource.CROSS_SLOT), stat());

        assertThat(writer.write(stats)).isEqualTo(3);

        List<Put> hostPuts = captured(HOST_TABLE);
        assertThat(hostPuts).hasSize(1);
        Put put = hostPuts.get(0);

        UidAppRowKey rowKey = UidAppRowKey.read(hasher.getSaltKey().size(), put.getRow());
        assertThat(rowKey.getApplicationName()).isEqualTo("p.checkout");
        assertThat(rowKey.getServiceUid()).isEqualTo(1);
        assertThat(rowKey.getTimestamp()).isEqualTo(LinkIndexKeys.slotStartMillis(slot));

        Cell cell = put.getFamilyCellMap().values().iterator().next().get(0);
        Buffer column = new FixedBuffer(CellUtil.cloneQualifier(cell));
        assertThat(column.readPrefixedString()).isEqualTo("cart:7070");
        assertThat(column.readPrefixedString()).isEqualTo("p.cart");
        assertThat(column.readInt()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(column.readInt()).isEqualTo(3);
    }

    @Test
    void nothingToWrite() {
        LinkMapWriter writer = new LinkMapWriter(hbaseOperations, tableNameProvider, hasher, new DefaultTimeSlot(), true, true);
        assertThat(writer.write(Map.of())).isZero();
        verify(hbaseOperations, org.mockito.Mockito.never()).execute(any(TableName.class), any());
    }

    @Test
    void destinationsOfOneInLinkRowAreMergedIntoOnePut() {
        LinkMapWriter writer = new LinkMapWriter(hbaseOperations, tableNameProvider, hasher, new DefaultTimeSlot(), true, true);
        Map<LinkKey, LinkStat> stats = new HashMap<>();
        stats.put(new LinkKey(callee, caller, "cart:7070", slot, LinkSource.SAME_SLOT), stat());
        stats.put(new LinkKey(callee, caller, "cart.svc:7070", slot, LinkSource.SAME_SLOT), stat());

        // one InLink row (the row key has no destinationId), two MapAppHost columns
        assertThat(writer.write(stats)).isEqualTo(3);

        List<Put> inPuts = captured(IN_TABLE);
        assertThat(inPuts).hasSize(1);
        HistogramSchema schema = ServiceType.OPENTELEMETRY_SERVER.getHistogramSchema();
        assertThat(columns(inPuts.get(0)))
                .containsEntry(key(ColumnNameV3.histogram(schema.findHistogramSlot(10, false).getSlotCode()).getColumnName()), 4L)
                .containsEntry(key(ColumnNameV3.histogram(schema.findHistogramSlot(4000, true).getSlotCode()).getColumnName()), 2L)
                .containsEntry(key(ColumnNameV3.histogram(schema.getSumStatSlot().getSlotCode()).getColumnName()), 8040L)
                .containsEntry(key(ColumnNameV3.histogram(schema.getMaxStatSlot().getSlotCode()).getColumnName()), 4000L);
        assertThat(captured(HOST_TABLE)).hasSize(2);
        // the inputs are not mutated by the merge
        assertThat(stats.values()).allSatisfy(stat -> assertThat(stat.getCount()).isEqualTo(3));
    }

    private static Map<String, Long> columns(Put put) {
        Map<String, Long> columns = new HashMap<>();
        for (List<Cell> cells : put.getFamilyCellMap().values()) {
            for (Cell cell : cells) {
                columns.put(key(CellUtil.cloneQualifier(cell)), Bytes.toLong(CellUtil.cloneValue(cell)));
            }
        }
        return columns;
    }

    private static String key(byte[] qualifier) {
        return Bytes.toStringBinary(qualifier);
    }
}

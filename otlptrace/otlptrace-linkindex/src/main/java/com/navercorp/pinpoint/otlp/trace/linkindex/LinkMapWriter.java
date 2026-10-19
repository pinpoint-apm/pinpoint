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

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.hbase.util.Puts;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.HostLinkFactory;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.InLinkFactory;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v3.ColumnNameV3;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v3.HostLinkFactoryV3;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v3.HostRowKeyEncoderV3;
import com.navercorp.pinpoint.common.server.applicationmap.statistics.v3.InLinkFactoryV3;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.trace.SlotCode;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Writes joined links to the server-map tables, in the same rows and columns the collector writes
 * for a call whose callee received the caller's Pinpoint parent application.
 * <ul>
 *     <li>InLink {@code callee <- caller} with a {@link LinkSource} subLink: <b>absolute</b> puts of the
 *     histogram, sum and max columns. The collector increments its rows; the join job owns the
 *     {@link LinkSource} rows and each row is written by exactly one job run, so overwriting keeps
 *     a re-run idempotent.</li>
 *     <li>MapAppHost {@code (caller, minute) -> (callee, destinationId)}: the web replaces
 *     {@code caller -> destinationId} with {@code caller -> callee}.</li>
 * </ul>
 * The InLink row does not carry the destinationId, so the statistics of the {@link LinkKey}s that differ
 * only by destinationId are merged into one put; the MapAppHost column keeps one entry per destinationId.
 * The puts are synchronous whatever {@code hbase.client.nativeAsync} says: the cleanup step deletes the
 * job's input once this returns, so a write failure has to fail the step.
 */
public class LinkMapWriter {

    private static final HbaseColumnFamily IN_LINK = HbaseTables.MAP_APP_IN;
    private static final HbaseColumnFamily HOST = HbaseTables.MAP_APP_HOST;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;
    private final ByteHasher hasher;
    private final InLinkFactory inLinkFactory;
    private final HostLinkFactory hostLinkFactory;
    private final boolean writeSum;
    private final boolean writeMax;

    /**
     * @param writeSum same value as the collector's {@code collector.map-link.avg.enable}
     * @param writeMax same value as the collector's {@code collector.map-link.max.enable}
     */
    public LinkMapWriter(HbaseOperations hbaseOperations,
                         TableNameProvider tableNameProvider,
                         ByteHasher hasher,
                         TimeSlot timeSlot,
                         boolean writeSum,
                         boolean writeMax) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.hasher = Objects.requireNonNull(hasher, "hasher");
        this.inLinkFactory = new InLinkFactoryV3(Objects.requireNonNull(timeSlot, "timeSlot"));
        this.hostLinkFactory = new HostLinkFactoryV3(new HostRowKeyEncoderV3(hasher));
        this.writeSum = writeSum;
        this.writeMax = writeMax;
    }

    /**
     * @return number of puts written
     */
    public int write(Map<LinkKey, LinkStat> stats) {
        if (stats.isEmpty()) {
            return 0;
        }
        // one InLink put per (callee, caller, minute, subLink): the row has no destinationId
        final Map<InLinkRow, LinkStat> inLinkRows = new LinkedHashMap<>();
        // one MapAppHost put per (caller, callee, destinationId, minute) whatever the subLink
        final Map<HostRow, LinkKey> hostRows = new LinkedHashMap<>();
        for (Map.Entry<LinkKey, LinkStat> entry : stats.entrySet()) {
            final LinkKey key = entry.getKey();
            inLinkRows.computeIfAbsent(InLinkRow.of(key), row -> new LinkStat(key.callee().serviceType().getHistogramSchema()))
                    .merge(entry.getValue());
            hostRows.putIfAbsent(HostRow.of(key), key);
        }
        final List<Put> inLinkPuts = new ArrayList<>(inLinkRows.size());
        for (Map.Entry<InLinkRow, LinkStat> entry : inLinkRows.entrySet()) {
            inLinkPuts.add(inLinkPut(entry.getKey(), entry.getValue()));
        }
        final List<Put> hostPuts = new ArrayList<>(hostRows.size());
        for (LinkKey key : hostRows.values()) {
            hostPuts.add(hostPut(key));
        }

        put(tableName(IN_LINK), inLinkPuts);
        put(tableName(HOST), hostPuts);
        return inLinkPuts.size() + hostPuts.size();
    }

    /**
     * Synchronous put: {@link HbaseOperations#put(TableName, List)} hands the puts to the async client
     * without waiting when {@code hbase.client.nativeAsync} is on, and the step must not complete before
     * the rows are acknowledged.
     */
    private void put(TableName tableName, List<Put> puts) {
        hbaseOperations.execute(tableName, table -> {
            table.put(puts);
            return null;
        });
    }

    Put inLinkPut(LinkKey key, LinkStat stat) {
        return inLinkPut(InLinkRow.of(key), stat);
    }

    Put inLinkPut(InLinkRow key, LinkStat stat) {
        final InLinkFactory.InLink inLink = inLinkFactory.newLink(key.callee(), key.caller(), key.source().getSubLink());
        final RowKey rowKey = inLink.rowkey(LinkIndexKeys.slotStartMillis(key.slot()));
        final byte[] row = hasher.writeSaltKey(rowKey.getRowKey(hasher.getSaltKey().size()));

        final Put put = new Put(row, true);
        final byte[] family = IN_LINK.getName();
        for (Map.Entry<SlotCode, Long> histogram : stat.getHistogram().entrySet()) {
            put.addColumn(family, ColumnNameV3.histogram(histogram.getKey()).getColumnName(), Bytes.toBytes(histogram.getValue()));
        }
        if (writeSum) {
            put.addColumn(family, inLink.sum().getColumnName(), Bytes.toBytes(stat.getSum()));
        }
        if (writeMax) {
            put.addColumn(family, inLink.max().getColumnName(), Bytes.toBytes(stat.getMax()));
        }
        return put;
    }

    Put hostPut(LinkKey key) {
        final byte[] row = hostLinkFactory.rowkey(key.caller(), LinkIndexKeys.slotStartMillis(key.slot()));
        final byte[] column = hostLinkFactory.columnName(key.callee(), key.destinationId());
        return Puts.put(row, HOST.getName(), column, null);
    }

    private TableName tableName(HbaseColumnFamily columnFamily) {
        return tableNameProvider.getTableName(columnFamily.getTable());
    }

    record InLinkRow(Vertex callee, Vertex caller, long slot, LinkSource source) {
        static InLinkRow of(LinkKey key) {
            return new InLinkRow(key.callee(), key.caller(), key.slot(), key.source());
        }
    }

    private record HostRow(Vertex caller, Vertex callee, String destinationId, long slot) {
        static HostRow of(LinkKey key) {
            return new HostRow(key.caller(), key.callee(), key.destinationId(), key.slot());
        }
    }
}

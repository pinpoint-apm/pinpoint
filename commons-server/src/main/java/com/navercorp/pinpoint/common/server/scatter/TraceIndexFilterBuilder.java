package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.filter.BinaryComponentComparator;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;

public class TraceIndexFilterBuilder {
    private static final long DEFAULT_ELAPSED_MIN = 0L;
    private static final long DEFAULT_ELAPSED_MAX = Integer.MAX_VALUE;
    private static final byte MIN_ELAPSED_BYTE = TraceIndexRowKeyUtils.toElapsedByte(DEFAULT_ELAPSED_MIN);

    private static final HbaseColumnFamily INDEX = HbaseTables.TRACE_INDEX;
    private static final HbaseColumnFamily META = HbaseTables.TRACE_INDEX_META;
    private static final byte[] META_QUALIFIER_RPC = HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC;
    private static final int ROW_FILTER_OFFSET = TraceIndexRowKeyUtils.SALTED_ROW_TIMESTAMP_OFFSET + 8 + 8;

    private long elapsedMin = DEFAULT_ELAPSED_MIN;
    private long elapsedMax = DEFAULT_ELAPSED_MAX;
    private Boolean success;
    private String agentId;
    private String rpcRegex;

    public TraceIndexFilterBuilder() {
    }

    public FilterList build(boolean enableAdditionalRowFilters, boolean enableValueFilter) {
        List<Filter> filters = new ArrayList<>();
        // row filters
        if (enableAdditionalRowFilters) {
            filters.addAll(createSuccessRowFilter(success));
            filters.addAll(createAgentIdHashRowFilter(agentId));
            filters.addAll(createElapsedByteRowFilter(elapsedMin, elapsedMax));
        }

        // value filters
        if (enableValueFilter) {
            filters.addAll(createElapsedValueFilter(elapsedMin, elapsedMax));
            filters.addAll(createRpcRegexValueFilter(rpcRegex));
        }
        return new FilterList(filters);
    }

    private List<Filter> createElapsedByteRowFilter(long elapsedMin, long elapsedMax) {
        if (elapsedMin <= DEFAULT_ELAPSED_MIN && elapsedMax >= DEFAULT_ELAPSED_MAX) {
            return List.of();
        }
        Byte minByte = null;
        Byte maxByte = null;
        if (elapsedMin > DEFAULT_ELAPSED_MIN) {
            minByte = TraceIndexRowKeyUtils.toElapsedByte(elapsedMin);
        }
        if (elapsedMax < DEFAULT_ELAPSED_MAX) {
            maxByte = TraceIndexRowKeyUtils.toElapsedByte(elapsedMax);
        }

        // if min and max are the same, use EQUAL filter
        if (minByte != null && minByte.equals(maxByte)) {
            return List.of(new RowFilter(CompareOperator.EQUAL, new BinaryComponentComparator(new byte[]{minByte}, ROW_FILTER_OFFSET)));
        }

        // otherwise, use range filters
        List<Filter> filters = new ArrayList<>(2);
        if (minByte != null && minByte > MIN_ELAPSED_BYTE) {
            filters.add(new RowFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComponentComparator(new byte[]{minByte}, ROW_FILTER_OFFSET)));
        }
        if (maxByte != null) {
            filters.add(new RowFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComponentComparator(new byte[]{maxByte}, ROW_FILTER_OFFSET)));
        }
        return filters;
    }

    private List<Filter> createSuccessRowFilter(Boolean success) {
        if (success == null) {
            return List.of();
        }
        // 0 for success
        BinaryComponentComparator comparator = new BinaryComponentComparator(new byte[]{0}, ROW_FILTER_OFFSET + 1); // elapsed(1)
        if (success) {
            return List.of(new RowFilter(CompareOperator.EQUAL, comparator));
        } else {
            return List.of(new RowFilter(CompareOperator.NOT_EQUAL, comparator));
        }
    }

    private List<Filter> createAgentIdHashRowFilter(String agentId) {
        if (agentId == null) {
            return List.of();
        }
        short agentIdHash = TraceIndexRowKeyUtils.toAgentIdHash(agentId);
        return List.of(new RowFilter(CompareOperator.EQUAL, new BinaryComponentComparator(Bytes.toBytes(agentIdHash), ROW_FILTER_OFFSET + 2))); // elapsed(1) + error(1)
    }

    private List<Filter> createElapsedValueFilter(long elapsedMin, long elapsedMax) {
        List<Filter> filters = new ArrayList<>();
        if (elapsedMin > DEFAULT_ELAPSED_MIN) {
            int yLow = (int) elapsedMin;
            filters.add(new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.GREATER_OR_EQUAL, new BinaryPrefixComparator(Bytes.toBytes(yLow))));
        }
        if (elapsedMax < DEFAULT_ELAPSED_MAX) {
            int yHigh = (int) elapsedMax;
            filters.add(new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.LESS_OR_EQUAL, new BinaryPrefixComparator(Bytes.toBytes(yHigh))));
        }
        return filters;
    }

    private List<Filter> createRpcRegexValueFilter(String rpcRegex) {
        if (rpcRegex == null) {
            return List.of();
        }
        return List.of(new SingleColumnValueFilter(META.getName(), META_QUALIFIER_RPC, CompareOperator.EQUAL, new RegexStringComparator(rpcRegex)));
    }

    public void setElapsedMin(long elapsedMin) {
        this.elapsedMin = elapsedMin;
    }

    public void setElapsedMax(long elapsedMax) {
        this.elapsedMax = elapsedMax;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void setRpcRegex(String rpcRegex) {
        this.rpcRegex = rpcRegex;
    }
}

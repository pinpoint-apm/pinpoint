package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
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
    private static final HbaseColumnFamily INDEX = HbaseTables.TRACE_INDEX;
    private static final HbaseColumnFamily META = HbaseTables.TRACE_INDEX_META;
    private static final byte[] META_QUALIFIER_RPC = HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC;
    private static final int ROW_FILTER_OFFSET = TraceIndexRowKeyUtils.SALTED_ROW_TIMESTAMP_OFFSET + 8 + 8;

    private LongPair elapsedMinMax;
    private Boolean success;
    private String agentId;
    private String rpcRegex;

    public TraceIndexFilterBuilder() {
    }

    public Filter build(boolean enableAdditionalRowFilters, boolean enableValueFilter) {
        List<Filter> filters = new ArrayList<>();
        // row filters
        if (enableAdditionalRowFilters) {
            if (success != null) {
                filters.add(createSuccessRowFilter(success));
            }
            if (agentId != null) {
                filters.add(createAgentIdHashRowFilter(agentId));
            }
            if (elapsedMinMax != null) {
                filters.add(createElapsedByteRowFilter(elapsedMinMax));
            }
        }

        // value filters
        if (enableValueFilter) {
            if (elapsedMinMax != null) {
                filters.add(createElapsedValueFilter(elapsedMinMax));
            }
            if (rpcRegex != null) {
                filters.add(createRpcRegexValueFilter(rpcRegex));
            }
        }
        return new FilterList(filters);
    }

    private Filter createElapsedByteRowFilter(LongPair elapsedMinMax) {
        List<Byte> allowedBytes = TraceIndexRowKeyUtils.toElapsedByteList(elapsedMinMax);
        if (allowedBytes.size() == 1) {
            return new RowFilter(CompareOperator.EQUAL, new BinaryComponentComparator(new byte[]{allowedBytes.get(0)}, ROW_FILTER_OFFSET));
        } else {
            return new FilterList(
                    new RowFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComponentComparator(new byte[]{allowedBytes.get(0)}, ROW_FILTER_OFFSET)),
                    new RowFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComponentComparator(new byte[]{allowedBytes.get(allowedBytes.size() - 1)}, ROW_FILTER_OFFSET))
            );
        }
    }

    private Filter createSuccessRowFilter(boolean success) {
        // 0 for success
        BinaryComponentComparator comparator = new BinaryComponentComparator(new byte[]{0}, ROW_FILTER_OFFSET + 1); // elapsed(1)
        if (success) {
            return new RowFilter(CompareOperator.EQUAL, comparator);
        } else {
            return new RowFilter(CompareOperator.NOT_EQUAL, comparator);
        }
    }

    private Filter createAgentIdHashRowFilter(String agentId) {
        short agentIdHash = TraceIndexRowKeyUtils.toAgentIdHash(agentId);
        return new RowFilter(CompareOperator.EQUAL, new BinaryComponentComparator(Bytes.toBytes(agentIdHash), ROW_FILTER_OFFSET + 2)); // elapsed(1) + error(1)
    }

    private Filter createElapsedValueFilter(LongPair elapsedMinMax) {
        int yLow = (int) elapsedMinMax.first();
        int yHigh = (int) elapsedMinMax.second();
        return new FilterList(
                new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.GREATER_OR_EQUAL, new BinaryPrefixComparator(Bytes.toBytes(yLow))),
                new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.LESS_OR_EQUAL, new BinaryPrefixComparator(Bytes.toBytes(yHigh)))
        );
    }

    private Filter createRpcRegexValueFilter(String rpcRegex) {
        return new SingleColumnValueFilter(META.getName(), META_QUALIFIER_RPC, CompareOperator.EQUAL, new RegexStringComparator(rpcRegex));
    }

    public void setElapsedMinMax(LongPair elapsedMinMax) {
        this.elapsedMinMax = elapsedMinMax;
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

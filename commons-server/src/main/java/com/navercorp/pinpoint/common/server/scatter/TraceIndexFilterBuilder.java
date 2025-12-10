package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.filter.BinaryComponentComparator;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TraceIndexFilterBuilder {
    private static final HbaseColumnFamily INDEX = HbaseTables.TRACE_INDEX;
    private static final HbaseColumnFamily META = HbaseTables.TRACE_INDEX_META;
    private static final byte[] META_QUALIFIER_RPC = HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC;

    private static final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();
    private static final FuzzyRowFilterFactory fuzzyRowFilter = new FuzzyRowFilterFactory();

    private final String applicationName;
    private LongPair elapsedMinMax;
    private Boolean success;
    private String agentId;
    private String rpcRegex;

    public TraceIndexFilterBuilder(String applicationName) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
    }

    public Filter build(boolean includeElapsed, boolean includeValueFilter) {
        List<Filter> filters = new ArrayList<>(5);

        // add row filter first
        if (!includeElapsed) {
            filters.add(createFuzzyRowFilter(applicationName, null));
        } else {
            filters.add(createFuzzyRowFilter(applicationName, elapsedMinMax));
        }

        // add value filters
        if (includeValueFilter) {
            if (success != null) {
                filters.add(createSuccessValueFilter(success));
            }
            if (elapsedMinMax != null) {
                filters.add(createElapsedValueFilter(elapsedMinMax));
            }
            if (agentId != null) {
                filters.add(createAgentIdValueFilter(agentId));
            }
            if (rpcRegex != null) {
                filters.add(createRpcRegexValueFilter(rpcRegex));
            }
        }
        return new FilterList(filters);
    }

    private Filter createFuzzyRowFilter(String applicationName, LongPair minMax) {
        AutomaticBuffer buffer = new AutomaticBuffer(1 + applicationName.length());
        buffer.putPrefixedString(applicationName);
        byte[] prefixedApplicationNameBytes = buffer.getBuffer();
        if (minMax == null) {
            return fuzzyRowFilter.build(prefixedApplicationNameBytes);
        }
        long yLow = minMax.first();
        long yHigh = minMax.second();
        List<Byte> allowedBytes = fuzzyRowKeyFactory.getRangeKey(yHigh, yLow);
        return fuzzyRowFilter.build(prefixedApplicationNameBytes, allowedBytes);
    }

    private Filter createSuccessValueFilter(Boolean success) {
        byte[] hasError = new byte[]{0};
        if (!success) {
            hasError[0] = 1; // 1 for failure/error (hasError flag)
        }
        return new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.EQUAL, new BinaryComponentComparator(hasError, 0));
    }

    private Filter createElapsedValueFilter(LongPair elapsedMinMax) {
        int yLow = (int) elapsedMinMax.first();
        int yHigh = (int) elapsedMinMax.second();
        return new FilterList(
                new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.GREATER_OR_EQUAL, new BinaryComponentComparator(Bytes.toBytes(yLow), 1)),
                new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.LESS_OR_EQUAL, new BinaryComponentComparator(Bytes.toBytes(yHigh), 1))
        );
    }

    private Filter createAgentIdValueFilter(String agentId) {
        Buffer buffer = new AutomaticBuffer(1 + 1 + agentId.length());
        buffer.putPrefixedString(agentId);
        byte[] prefixedAgentId = buffer.getBuffer();
        return new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.EQUAL, new BinaryComponentComparator(prefixedAgentId, 1 + 4));
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

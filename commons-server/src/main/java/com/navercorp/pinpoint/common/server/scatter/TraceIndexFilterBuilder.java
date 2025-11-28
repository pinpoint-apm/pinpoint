package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TraceIndexFilterBuilder {
    private static final HbaseColumnFamily INDEX = HbaseTables.TRACE_INDEX;
    private static final HbaseColumnFamily META = HbaseTables.TRACE_INDEX_META;
    private static final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();
    private static final FuzzyRowFilterFactory fuzzyRowFilter = new FuzzyRowFilterFactory();

    private final String applicationName;
    private LongPair elapsedMinMax;
    private Boolean success;
    private String agentId;

    public TraceIndexFilterBuilder(String applicationName) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
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

    public Filter build(boolean includeElapsed, boolean includeValueFilter) {
        AutomaticBuffer buffer = new AutomaticBuffer(1 + applicationName.length());
        buffer.putPrefixedString(applicationName);
        byte[] prefixedApplicationNameBytes = buffer.getBuffer();

        Filter rowFilter;
        if (!includeElapsed) {
            rowFilter = createFuzzyRowFilter(prefixedApplicationNameBytes, null);
        } else {
            rowFilter = createFuzzyRowFilter(prefixedApplicationNameBytes, elapsedMinMax);
        }

        if (!includeValueFilter) {
            return rowFilter;
        }
        Filter valueFilter = createValueFilter();
        if (valueFilter == null) {
            return rowFilter;
        }
        return new FilterList(rowFilter, valueFilter); // rowFilter should be applied first
    }

    private Filter createFuzzyRowFilter(byte[] prefixedApplicationNameBytes, LongPair minMax) {
        if (minMax == null) {
            return fuzzyRowFilter.build(prefixedApplicationNameBytes);
        }
        long yLow = minMax.first();
        long yHigh = minMax.second();
        List<Byte> allowedBytes = fuzzyRowKeyFactory.getRangeKey(yHigh, yLow);
        return fuzzyRowFilter.build(prefixedApplicationNameBytes, allowedBytes);
    }

    private Filter createValueFilter() {
        if (success == null && agentId == null) {
            return null;
        }

        if (success != null && agentId == null) {
            // only success is specified
            byte[] valuePrefix = new byte[1];
            if (!success) {
                valuePrefix[0] = 1; // 1 for failure/error (hasError flag)
            }
            return new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.EQUAL, new BinaryPrefixComparator(valuePrefix));
        } else if (success == null) {
            // only agentId is specified
            byte[] successValuePrefix = getAgentIdPrefix(agentId);
            byte[] failureValuePrefix = Arrays.copyOf(successValuePrefix, successValuePrefix.length);
            failureValuePrefix[0] = 1;

            return new FilterList(FilterList.Operator.MUST_PASS_ONE,
                    new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.EQUAL, new BinaryPrefixComparator(successValuePrefix)),
                    new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.EQUAL, new BinaryPrefixComparator(failureValuePrefix))
            );
        } else {
            // both success and agentId are specified
            byte[] valuePrefix = getAgentIdPrefix(agentId);
            if (!success) {
                valuePrefix[0] = 1;
            }
            return new SingleColumnValueFilter(INDEX.getName(), INDEX.getName(), CompareOperator.EQUAL, new BinaryPrefixComparator(valuePrefix));
        }
    }

    private byte[] getAgentIdPrefix(String agentId) {
        Buffer buffer = new AutomaticBuffer(1 + agentId.length() + 1);
        buffer.putByte((byte) 0);
        buffer.putPrefixedString(agentId);
        return buffer.getBuffer();
    }
}

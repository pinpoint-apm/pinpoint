package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.util.TimestampUtils;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.web.dao.ApplicationIndexPerTimeDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class HbaseApplicationIndexPerTimeDao implements ApplicationIndexPerTimeDao {

    private static final HbaseColumnFamily.ApplicationIndexPerTime DESCRIPTOR = HbaseColumnFamily.APPLICATION_INDEX_PER_TIME;
    private static final int SCANNER_CACHE_SIZE = 1;

    private final HbaseOperations2 hbaseOperations2;
    private final TableNameProvider tableNameProvider;

    private final ResultsExtractor<List<String>> agentIdPerTimeExtractor;


    public HbaseApplicationIndexPerTimeDao(HbaseOperations2 hbaseOperations2,
                                           TableNameProvider tableNameProvider,
                                           @Qualifier("agentIdPerTimeExtractor") ResultsExtractor<List<String>> agentIdPerTimeExtractor) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.agentIdPerTimeExtractor = Objects.requireNonNull(agentIdPerTimeExtractor, "agentIdPerTimeExtractor");
    }

    @Override
    public List<String> selectAgentIds(String applicationName, Range range) {
        return selectApplicationIndex0(applicationName, range, agentIdPerTimeExtractor);
    }


    private <T> List<T> selectApplicationIndex0(String applicationName, Range range, ResultsExtractor<List<T>> resultsExtractor) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(resultsExtractor, "resultsExtractor");

        Scan scan = new Scan();
        scan.setMaxVersions(1);
        scan.setCaching(SCANNER_CACHE_SIZE);

        scan.withStartRow(createRowKey(applicationName, range.getTo(), EarlierLater.LATER));
        scan.withStopRow(createRowKey(applicationName, range.getFrom(), EarlierLater.EARLIER), true);
        scan.addFamily(DESCRIPTOR.getName());

        TableName applicationIndexTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations2.find(applicationIndexTableName, scan, resultsExtractor);
    }

    private enum EarlierLater {
        EARLIER(-1),
        LATER(+1);

        private final long value;

        EarlierLater(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }

    private byte[] createRowKey(String applicationName, long timeMillis, EarlierLater earlierLater) {
        long timeslotSize = TimestampUtils.getTimeslotSize();
        long paddedTimeMillis = timeMillis + timeslotSize * earlierLater.getValue();

        return createRowKey(applicationName, paddedTimeMillis);
    }

    private byte[] createRowKey(String applicationName, long timeMillis) {
        // rowKey = applicationName + roundedTimestamp (reversed)
        byte[] applicationNameKey = Bytes.toBytes(applicationName);
        byte[] currentRoundedTimestamp = Bytes.toBytes(TimestampUtils.reverseRoundedTimeMillis(timeMillis));

        byte[] rowKey = new byte[HbaseTableConstants.APPLICATION_NAME_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH];
        BytesUtils.writeBytes(rowKey, 0, applicationNameKey);
        int offset = HbaseTableConstants.APPLICATION_NAME_MAX_LEN;
        BytesUtils.writeBytes(rowKey, offset, currentRoundedTimestamp);

        return rowKey;
    }
}

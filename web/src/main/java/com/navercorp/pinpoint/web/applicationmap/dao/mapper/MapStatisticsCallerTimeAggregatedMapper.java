package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * rowkey = caller col = callee
 */
@Component
public class MapStatisticsCallerTimeAggregatedMapper implements RowMapper<LinkDataMap> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkFilter filter;


    @Autowired
    private ApplicationFactory applicationFactory;

    @Autowired
    @Qualifier("statisticsCallerRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    public MapStatisticsCallerTimeAggregatedMapper() {
        this(LinkFilter::skip);
    }

    public MapStatisticsCallerTimeAggregatedMapper(LinkFilter filter) {
        this.filter = Objects.requireNonNull(filter, "filter");
    }

    @Override
    public LinkDataMap mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new LinkDataMap();
        }
        logger.debug("mapRow:{}", rowNum);

        final byte[] rowKey = getOriginalKey(result.getRow());

        final Buffer row = new FixedBuffer(rowKey);
        final Application caller = readCallerApplication(row);
        final long timestamp = 0; //aggregate timestamp

        // key is destApplicationName.
        final LinkDataMap linkDataMap = new LinkDataMap();
        for (Cell cell : result.rawCells()) {
            final Buffer buffer = new OffsetFixedBuffer(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            final Application callee = readCalleeApplication(buffer);
            if (filter.filter(callee)) {
                continue;
            }

            String calleeHost = buffer.readPrefixedString();
            short histogramSlot = buffer.readShort();

            boolean isError = histogramSlot == (short) -1;

            String callerAgentId = buffer.readPrefixedString();

            long requestCount = CellUtils.valueToLong(cell);
            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Caller.(New) {} {} -> {} (slot:{}/{}) calleeHost:{}", caller, callerAgentId, callee, histogramSlot, requestCount, calleeHost);
            }

            final short slotTime = (isError) ? (short) -1 : histogramSlot;
            if (StringUtils.isEmpty(calleeHost)) {
                calleeHost = callee.getName();
            }
            linkDataMap.addLinkData(caller, callerAgentId, callee, calleeHost, timestamp, slotTime, requestCount);
        }

        return linkDataMap;
    }


    private Application readCalleeApplication(Buffer buffer) {
        short calleeServiceType = buffer.readShort();
        String calleeApplicationName = buffer.readPrefixedString();
        return applicationFactory.createApplication(calleeApplicationName, calleeServiceType);
    }

    private Application readCallerApplication(Buffer row) {
        String callerApplicationName = row.read2PrefixedString();
        short callerServiceType = row.readShort();
        return this.applicationFactory.createApplication(callerApplicationName, callerServiceType);
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }
}

package com.nhn.hippo.web.dao;

import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.BytesUtils;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Repository
public class HbaseTraceIndexDao implements TraceIndexDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final byte[] COLFAM_TRACE = Bytes.toBytes("Trace");
    private final byte[] COLNAME_ID = Bytes.toBytes("ID");

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("traceIndexMapper")
    private RowMapper<byte[]> traceIndexMapper;


    private int scanCacheSize = 20;

    public void setScanCacheSize(int scanCacheSize) {
        this.scanCacheSize = scanCacheSize;
    }


    @Override
    public List<byte[]> scanTraceIndex(String agent, long start, long end) {
        Scan scan = createScan(agent, start, end);
        return hbaseOperations2.find(HBaseTables.TRACE_INDEX, scan, traceIndexMapper);
    }

    @Override
    public List<List<byte[]>> multiScanTraceIndex(String[] agents, long start, long end) {
        final List<Scan> multiScan = new ArrayList<Scan>(agents.length);
        for (String agent : agents) {
            Scan scan = createScan(agent, start, end);
            multiScan.add(scan);
        }
        return hbaseOperations2.find(HBaseTables.TRACE_INDEX, multiScan, traceIndexMapper);
    }


    private Scan createScan(String agent, long start, long end) {
        byte[] bAgent = Bytes.toBytes(agent);

        Scan scan = new Scan();
        // cache size를 지정해야 되는거 같음.??
        scan.setCaching(this.scanCacheSize);

        byte[] bStart = BytesUtils.add(bAgent, start);
        scan.setStartRow(bStart);

        byte[] bEnd = BytesUtils.add(bAgent, end);
        scan.setStopRow(bEnd);

        scan.addColumn(COLFAM_TRACE, COLNAME_ID);

        // json으로 변화해서 로그를 찍어서. 최초 변환 속도가 느림.
        logger.debug("create scan:{}", scan);
        return scan;
    }


    @Override
    public List parallelScanTraceIndex(String[] agents, long start, long end) {
        throw new UnsupportedOperationException();
    }
}

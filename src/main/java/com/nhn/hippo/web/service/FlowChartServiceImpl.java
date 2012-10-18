package com.nhn.hippo.web.service;

import com.nhn.hippo.web.calltree.rpc.RPCCallTree;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.dao.TraceDao;
import com.nhn.hippo.web.dao.TraceIndexDao;
import com.nhn.hippo.web.service.TracesProcessor.SpanHandler;
import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.hbase.HBaseQuery;
import com.profiler.common.hbase.HBaseQuery.HbaseColumn;
import com.profiler.common.hbase.HBaseTables;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author netspider
 */
@Service
public class FlowChartServiceImpl implements FlowChartService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("hbaseClient")
    HBaseClient client;

    @Autowired
    private TraceDao traceDao;

    @Autowired
    private TraceIndexDao traceIndexDao;

    @Override
    public String[] selectAgentIds(String[] hosts) {
        List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
        column.add(new HbaseColumn("Agents", "AgentID"));

        HBaseQuery query = new HBaseQuery(HBaseTables.SERVERS, null, null, column);
        Iterator<Map<String, byte[]>> iterator = client.getHBaseData(query);

        if (logger.isDebugEnabled()) {
            while (iterator.hasNext()) {
                logger.debug("selectedAgentId={}", iterator.next());
            }
            logger.debug("!!!==============WARNING==============!!!");
            logger.debug("!!! selectAgentIds IS NOT IMPLEMENTED !!!");
            logger.debug("!!!===================================!!!");
        }

        return hosts;
    }

    @Override
    public Set<TraceId> selectTraceIdsFromTraceIndex(String[] agentIds, long from, long to) {
        if (agentIds == null) {
            throw new NullPointerException("agentIds");
        }

        if (agentIds.length == 1) {
            // single scan
            if (logger.isTraceEnabled()) {
                logger.trace("scan {}, {}, {}", new Object[]{agentIds[0], from, to});
            }
            List<byte[]> bytes = this.traceIndexDao.scanTraceIndex(agentIds[0], from, to);
            // 이런 필터로직을 scan filter에서 할수 없나?
            Set<TraceId> result = new HashSet<TraceId>();
            for (byte[] traceId : bytes) {
                TraceId tid = new TraceId(traceId);
                result.add(tid);
                logger.trace("traceid:{}", tid);
            }
            return result;
        } else {
            // multi scan 가능한 동일 htable 에서 액세스함.
            List<List<byte[]>> multiScan = this.traceIndexDao.multiScanTraceIndex(agentIds, from, to);
            Set<TraceId> result = new HashSet<TraceId>();
            for (List<byte[]> scan : multiScan) {
                for (byte[] traceId : scan) {
                    result.add(new TraceId(traceId));
                }
            }
            return result;
        }
    }

    @Override
    public Map<byte[], List<Span>> selectTraces(List<byte[]> traceIds) {
        List<Get> gets = new ArrayList<Get>(traceIds.size());
        for (byte[] traceId : traceIds) {
            gets.add(new Get(traceId));
        }

        Result[] results = client.get(HBaseTables.TRACES, gets);

        // traceId, SpanList
        final Map<byte[], List<Span>> result = new HashMap<byte[], List<Span>>();

        TracesProcessor.process(results, new SpanHandler() {
            @Override
            public void handleSpan(byte[] row, byte[] family, byte[] column, Span span) {
                if (result.containsKey(row)) {
                    result.get(row).add(span);
                } else {
                    List<Span> list = new ArrayList<Span>();
                    list.add(span);
                    result.put(row, list);
                }
            }
        });

        return result;
    }

    @Override
    public RPCCallTree selectRPCCallTree(Set<TraceId> traceIds) {
        final RPCCallTree tree = new RPCCallTree();
        List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);
        for (List<SpanBo> transaction : traces) {
            for (SpanBo eachTransaction : transaction) {
                tree.addSpan(eachTransaction);
            }
        }
        return tree.build();
    }

    @Override
    public ServerCallTree selectServerCallTree(Set<TraceId> traceIds) {
        final ServerCallTree tree = new ServerCallTree();

        List<List<SpanBo>> traces = this.traceDao.selectSpansAndAnnotation(traceIds);

        for (List<SpanBo> transaction : traces) {
            for (SpanBo eachTransaction : transaction) {
                tree.addSpan(eachTransaction);
            }
        }
        return tree.build();
    }

}

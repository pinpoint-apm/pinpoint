package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.nhn.hippo.web.calltree.rpc.RPCCallTree;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.dao.ApplicationIndexDao;
import com.nhn.hippo.web.dao.ApplicationTraceIndexDao;
import com.nhn.hippo.web.dao.RootTraceIndexDao;
import com.nhn.hippo.web.dao.TraceDao;
import com.nhn.hippo.web.dao.TraceIndexDao;
import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.hbase.HBaseQuery;
import com.profiler.common.hbase.HBaseQuery.HbaseColumn;
import com.profiler.common.hbase.HBaseTables;

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
    private RootTraceIndexDao rootTraceIndexDao;

    @Autowired
    private TraceIndexDao traceIndexDao;

    @Autowired
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Override
    public List<String> selectAllApplicationNames() {
        return applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    public String[] selectAgentIdsFromApplicationName(String applicationName) {
        return applicationIndexDao.selectAgentIds(applicationName);
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
            Set<TraceId> result = new HashSet<TraceId>();
            for (byte[] traceId : bytes) {
                TraceId tid = new TraceId(traceId);
                result.add(tid);
                logger.trace("traceid:{}", tid);
            }
            return result;
        } else {
            // multi scan 가능한 동일 open htable 에서 액세스함.
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

        List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);

        for (List<SpanBo> transaction : traces) {
            List<SpanBo> processed = refine(transaction);
            markRecursiveCall(processed);
            for (SpanBo eachTransaction : processed) {
                tree.addSpan(eachTransaction);
            }
        }
        return tree.build();
    }

	private SpanBo findChildSpan(final List<SpanBo> list, final SpanBo parent) {
		for (int i = 0; i < list.size(); i++) {
			SpanBo child = list.get(i);

			if (child.getParentSpanId() == parent.getSpanId()) {
				return child;
			}
		}
		return null;
	}

	private List<SpanBo> refine(final List<SpanBo> list) {
		for (int i = 0; i < list.size(); i++) {
			SpanBo span = list.get(i);
			String svcName = span.getServiceName();

			// TODO 임시로 HTTP/1.1을 확인하게 해두었음. merge해야하는 span 확인 방법을 바꿔야함.
			if ("HTTP/1.1".equals(svcName)) {
				SpanBo child = findChildSpan(list, span);

				if (child != null) {
					child.setParentSpanId(span.getParentSpanId());
					child.getAnnotationBoList().addAll(span.getAnnotationBoList());
					list.remove(i);
					i--;
					continue;
				} else {
					// using as a terminal node.
					span.setServiceName(span.getEndPoint());
				}
			}
		}
		return list;
	}
    
    private void markRecursiveCall(final List<SpanBo> list) {
        for (int i = 0; i < list.size(); i++) {
            SpanBo a = list.get(i);
            for (int j = 0; j < list.size(); j++) {
                if (i == j)
                    continue;
                SpanBo b = list.get(j);
                if (a.getServiceName().equals(b.getServiceName()) && a.getSpanId() == b.getParentSpanId()) {
                    a.increaseRecursiveCallCount();
                }
            }
        }
    }

    @Override
    public Set<TraceId> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("scan {}, {}, {}", new Object[]{applicationName, from, to});
        }

        List<byte[]> bytes = this.applicationTraceIndexDao.scanTraceIndex(applicationName, from, to);
        Set<TraceId> result = new HashSet<TraceId>();
        for (byte[] traceId : bytes) {
            TraceId tid = new TraceId(traceId);
            result.add(tid);
            logger.trace("traceid:{}", tid);
        }
        return result;
    }

    @Override
    public String[] selectAgentIds(String[] hosts) {
        List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
        column.add(new HbaseColumn("Agents", "AgentID"));

        HBaseQuery query = new HBaseQuery(HBaseTables.APPLICATION_INDEX, null, null, column);
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
}

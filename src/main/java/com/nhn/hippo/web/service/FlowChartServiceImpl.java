package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.nhn.hippo.web.calltree.RPCCallTree;
import com.nhn.hippo.web.calltree.ServerCallTree;
import com.nhn.hippo.web.service.TracesProcessor.SpanHandler;
import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.hbase.HBaseQuery;
import com.profiler.common.hbase.HBaseQuery.HbaseColumn;
import com.profiler.common.hbase.HBaseTables;

/**
 * 
 * @author netspider
 * 
 */
@Service
public class FlowChartServiceImpl implements FlowChartService {

	@Autowired
	@Qualifier("hbaseClient")
	HBaseClient client;

	@Override
	public String[] selectAgentIds(String[] hosts) {
		List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
		column.add(new HbaseColumn("Agents", "AgentID"));

		HBaseQuery query = new HBaseQuery(HBaseTables.SERVERS, null, null, column);
		Iterator<Map<String, byte[]>> iterator = client.getHBaseData(query);

		while (iterator.hasNext()) {
			System.out.println("selectedAgentId=" + iterator.next());
		}

		System.out.println("!!!==============WARNING==============!!!");
		System.out.println("!!! selectAgentIds IS NOT IMPLEMENTED !!!");
		System.out.println("!!!===================================!!!");

		return hosts;
	}

	@Override
	public Set<TraceId> selectTraceIdsFromTraceIndex(String[] agentIds, long from, long to) {
		List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
		column.add(new HbaseColumn("Trace", "ID"));

		Set<TraceId> set = new HashSet<TraceId>();

		for (String agentId : agentIds) {
			byte[] s = ArrayUtils.addAll(Bytes.toBytes(agentId), Bytes.toBytes(from));
			byte[] e = ArrayUtils.addAll(Bytes.toBytes(agentId), Bytes.toBytes(to));

			HBaseQuery query = new HBaseQuery(HBaseTables.TRACE_INDEX, s, e, column);
			Iterator<Map<String, byte[]>> result = client.getHBaseData(query);

			while (result.hasNext()) {
				set.add(new TraceId(result.next().get("ID")));
			}
		}

		return set;
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
		List<Get> gets = new ArrayList<Get>(traceIds.size());
		for (TraceId traceId : traceIds) {
			gets.add(new Get(traceId.getBytes()));
		}

		Result[] results = client.get(HBaseTables.TRACES, gets);

		final RPCCallTree tree = new RPCCallTree();

		TracesProcessor.process(results, new SpanHandler() {
			@Override
			public void handleSpan(byte[] row, byte[] family, byte[] column, Span span) {
				tree.addSpan(span);
			}
		});

		return tree.build();
	}

	@Override
	public ServerCallTree selectServerCallTree(List<byte[]> traceIds) {
		List<Get> gets = new ArrayList<Get>(traceIds.size());
		for (byte[] traceId : traceIds) {
			gets.add(new Get(traceId));
		}

		Result[] results = client.get(HBaseTables.TRACES, gets);

		final ServerCallTree tree = new ServerCallTree();

		TracesProcessor.process(results, new SpanHandler() {
			@Override
			public void handleSpan(byte[] row, byte[] family, byte[] column, Span span) {
				tree.addSpan(span);
			}
		});

		return tree.build();
	}
}

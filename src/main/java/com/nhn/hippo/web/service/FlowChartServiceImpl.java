package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

		return null;
	}

	@Override
	public List<byte[]> selectTraceIdsFromTraceIndex(String[] agentIds, long from, long to) {
		List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
		column.add(new HbaseColumn("Trace", "ID"));

		List<byte[]> list = new ArrayList<byte[]>();

		for (String agentId : agentIds) {
			byte[] s = ArrayUtils.addAll(Bytes.toBytes(agentId), Bytes.toBytes(from));
			byte[] e = ArrayUtils.addAll(Bytes.toBytes(agentId), Bytes.toBytes(to));

			HBaseQuery query = new HBaseQuery(HBaseTables.TRACE_INDEX, s, e, column);
			Iterator<Map<String, byte[]>> result = client.getHBaseData(query);

			while (result.hasNext()) {
				list.add(result.next().get("ID"));
			}
		}

		return list;
	}

	@Override
	public Map<byte[], List<Span>> selectTraces(List<byte[]> traceIds) {
		List<Get> gets = new ArrayList<Get>(traceIds.size());
		for (byte[] traceId : traceIds) {
			gets.add(new Get(traceId));
		}

		Result[] results = client.get(HBaseTables.TRACES, gets);

		Map<byte[], List<Span>> result = new HashMap<byte[], List<Span>>();

		for (Result r : results) {
			result.put(r.getRow(), populateSpans(r));
		}

		System.out.println("result=" + result);

		return result;
	}

	private List<Span> populateSpans(Result res) {
		List<Span> list = new ArrayList<Span>();

		NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = res.getMap();

		for (Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> entry : map.entrySet()) {
			byte[] family = entry.getKey();
			System.out.println("family=" + Bytes.toString(family));

			NavigableMap<byte[], NavigableMap<Long, byte[]>> values = entry.getValue();

			for (Entry<byte[], NavigableMap<Long, byte[]>> value : values.entrySet()) {
				byte[] colname = value.getKey();
				System.out.println("colname=" + Bytes.toString(colname));

				NavigableMap<Long, byte[]> valueSeries = value.getValue();

				for (Entry<Long, byte[]> v : valueSeries.entrySet()) {
					Span span = new Span();
					try {
						new TDeserializer().deserialize(span, v.getValue());
						list.add(span);
					} catch (TException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return list;
	}

}

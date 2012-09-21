package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.hbase.HBaseQuery;
import com.profiler.common.hbase.HBaseQuery.HbaseColumn;
import com.profiler.common.hbase.HBaseTables;

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
		Iterator<Map<String, Object>> iterator = client.getHBaseData(query);

		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}

		return null;
	}

	@Override
	public Iterator<Map<String, Object>> selectTraces(String[] agentIds, long from, long to) {
		List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
		column.add(new HbaseColumn("Trace", "ID"));

		final List<Iterator<Map<String, Object>>> list = new ArrayList<Iterator<Map<String, Object>>>();

		for (String agentId : agentIds) {
			byte[] s = ArrayUtils.addAll(Bytes.toBytes(agentId), Bytes.toBytes(from));
			byte[] e = ArrayUtils.addAll(Bytes.toBytes(agentId), Bytes.toBytes(to));

			HBaseQuery query = new HBaseQuery(HBaseTables.TRACE_INDEX, s, e, column);
			Iterator<Map<String, Object>> result = client.getHBaseData(query);
			list.add(result);
		}

		return new Iterator<Map<String, Object>>() {
			int pos = 0;

			@Override
			public boolean hasNext() {
				if (list.isEmpty())
					return false;

				while (pos < list.size()) {
					if (list.get(pos).hasNext()) {
						break;
					} else {
						pos++;

						if (pos >= list.size()) {
							return false;
						} else {
							continue;
						}
					}
				}
				return true;
			}

			@Override
			public Map<String, Object> next() {
				return list.get(pos).next();
			}

			@Override
			public void remove() {
				throw new RuntimeException("NOT SUPPORTED METHOD");
			}
		};
	}
}

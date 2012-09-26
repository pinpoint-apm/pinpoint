package com.nhn.hippo.web.service;

import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.Result;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import com.nhn.hippo.web.calltree.RPCCallTree;
import com.profiler.common.dto.thrift.Span;

public class TracesProcessor {

	public static RPCCallTree process(Result[] results) {
		RPCCallTree callTree = new RPCCallTree();

		TDeserializer deserializer = new TDeserializer();

		for (Result res : results) {
			/**
			 * res.getMap() represent
			 * "Map<FAMILY, Map<COLUMN_NAME, Map<Timestamp, VALUE>>>"
			 */
			NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = res.getMap();

			for (Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> entry : map.entrySet()) {
				NavigableMap<byte[], NavigableMap<Long, byte[]>> values = entry.getValue();

				/**
				 * For each column (SpanID)
				 */
				for (Entry<byte[], NavigableMap<Long, byte[]>> value : values.entrySet()) {
					NavigableMap<Long, byte[]> valueSeries = value.getValue();

					/**
					 * Decode span object
					 */
					for (Entry<Long, byte[]> v : valueSeries.entrySet()) {
						Span span = new Span();
						try {
							deserializer.deserialize(span, v.getValue());
							callTree.addSpan(span);
						} catch (TException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		return callTree.build();
	}
}

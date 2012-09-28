package com.nhn.hippo.web.service;

import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import com.profiler.common.dto.thrift.Span;

/**
 * 
 * @author netspider
 * 
 */
public class TracesProcessor {

	public static interface SpanHandler {
		void handleSpan(byte[] row, byte[] family, byte[] column, Span span);
	}

	public static void process(Result[] results, SpanHandler handler) {
		TDeserializer deserializer = new TDeserializer();

		for (Result res : results) {
			/**
			 * res.getMap() represent
			 * "Map<FAMILY, Map<COLUMN_NAME, Map<Timestamp, VALUE>>>"
			 */
			NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = res.getMap();

			for (Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> entry : map.entrySet()) {
				byte[] family = entry.getKey();
				System.out.println("family=" + Bytes.toString(family));

				NavigableMap<byte[], NavigableMap<Long, byte[]>> values = entry.getValue();

				/**
				 * For each column (SpanID)
				 */
				for (Entry<byte[], NavigableMap<Long, byte[]>> value : values.entrySet()) {
					byte[] colname = value.getKey();
					System.out.println("colname=" + Bytes.toString(colname));

					NavigableMap<Long, byte[]> valueSeries = value.getValue();

					/**
					 * Decode span object
					 */
					for (Entry<Long, byte[]> v : valueSeries.entrySet()) {
						Span span = new Span();
						try {
							deserializer.deserialize(span, v.getValue());
							handler.handleSpan(res.getRow(), family, colname, span);
						} catch (TException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}

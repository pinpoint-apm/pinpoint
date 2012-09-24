package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import junit.framework.Assert;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.profiler.common.hbase.HBaseClient;

public class FlowChartServiceImplTest {

	private static final String TABLE_NAME = "ttt";

	private static HBaseClient client;

	@BeforeClass
	public static void init() {
		client = new HBaseClient("localhost", "2181", 2);
		Assert.assertNotNull(client);
	}

	@AfterClass
	public static void destroy() {
		client.close();
	}

	@Test
	public void hbase() {
		List<Get> gets = new ArrayList<Get>(1);
		gets.add(new Get("row1".getBytes()));

		Result[] results = client.get(TABLE_NAME, gets);

		for (Result r : results) {
			NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = r.getMap();

			for (Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> entry : map.entrySet()) {
				byte[] family = entry.getKey();
				System.out.println("family=" + Bytes.toString(family));

				NavigableMap<byte[], NavigableMap<Long, byte[]>> values = entry.getValue();

				for (Entry<byte[], NavigableMap<Long, byte[]>> value : values.entrySet()) {
					byte[] colname = value.getKey();
					System.out.println("colname=" + Bytes.toString(colname));

					NavigableMap<Long, byte[]> valueSeries = value.getValue();

					for (Entry<Long, byte[]> v : valueSeries.entrySet()) {
						byte[] vv = v.getValue();
						System.out.println("value=" + Bytes.toString(vv));
					}
				}
			}
		}
	}
}

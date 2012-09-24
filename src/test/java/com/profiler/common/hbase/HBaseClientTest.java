package com.profiler.common.hbase;

import java.util.*;

import com.profiler.common.util.PropertyUtils;
import junit.framework.Assert;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.profiler.common.hbase.HBaseQuery.HbaseColumn;

public class HBaseClientTest {

	private static final String TABLE_NAME = "TEST_TABLE";
	private static final String COLUMN_FAMILY = "COLUMN_FAMILY";
	private static HBaseClient client;

	@BeforeClass
	public static void init() {
        Properties properties = PropertyUtils.readProperties("test-hbase.properties");
        client = new HBaseClient(properties);
		if (client.isTableExists(TABLE_NAME)) {
			client.dropTable(TABLE_NAME);
		}
		Assert.assertNotNull(client);
	}

	@AfterClass
	public static void destroy() {
		if (client.isTableExists(TABLE_NAME)) {
			client.dropTable(TABLE_NAME);
		}
		client.close();
	}

	@Test
	public void manageTable() {
		client.createTable(new HTableDescriptor(TABLE_NAME));

		Assert.assertTrue("Table is not exists", client.isTableExists(TABLE_NAME));

		client.dropTable(TABLE_NAME);
		client.isTableExists(TABLE_NAME);

		Assert.assertFalse("Table is not dropped", client.isTableExists(TABLE_NAME));
	}

	@Test
	public void insertRow() {
		HTableDescriptor desc = new HTableDescriptor(TABLE_NAME);
		desc.addFamily(new HColumnDescriptor(COLUMN_FAMILY));
		client.createTable(desc);
		Assert.assertTrue("Table is not exists", client.isTableExists(TABLE_NAME));

		List<Put> putList = new ArrayList<Put>();
		for (int i = 0; i < 10; i++) {
			Put put = new Put(Bytes.toBytes(i + "row"));
			put.add(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("qual1"), Bytes.toBytes("val1"));
			put.add(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("qual2"), Bytes.toBytes("val2"));
			putList.add(put);
		}
		client.insert(TABLE_NAME, putList);

		List<HbaseColumn> list = new ArrayList<HBaseQuery.HbaseColumn>();
		list.add(new HbaseColumn(COLUMN_FAMILY, "qual1"));
		list.add(new HbaseColumn(COLUMN_FAMILY, "qual2"));
		Iterator<Map<String, byte[]>> result = client.getHBaseData(new HBaseQuery(TABLE_NAME, Bytes.toBytes("0"), Bytes.toBytes("9"), list));

		while (result.hasNext()) {
			Map<String, byte[]> next = result.next();
			System.out.println(next);
		}
	}
}

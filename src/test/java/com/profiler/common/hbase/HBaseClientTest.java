package com.profiler.common.hbase;

import com.profiler.common.hbase.HBaseQuery.HbaseColumn;
import com.profiler.common.util.PropertyUtils;
import junit.framework.Assert;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class HBaseClientTest {

    private static final String TABLE_NAME = "TEST_TABLE";
    private static final String COLUMN_FAMILY = "COLUMN_FAMILY";
    private static HBaseClient client;

    private static HBaseAdminTemplate adminTemplate;

    @BeforeClass
    public static void init() throws IOException {
        URL resource = HBaseClientTest.class.getClassLoader().getResource("test-hbase.properties");
        Properties properties = PropertyUtils.readProperties(resource.getPath());
        client = new HBaseClient(properties);
        adminTemplate = new HBaseAdminTemplate(client.getConfiguration());
        adminTemplate.dropTableIfExist(TABLE_NAME);

        Assert.assertNotNull(client);
    }

    @AfterClass
    public static void destroy() {
        adminTemplate.dropTableIfExist(TABLE_NAME);
        client.close();
    }

    @Test
    public void manageTable() {
        adminTemplate.createTableIfNotExist(new HTableDescriptor(TABLE_NAME));

        Assert.assertTrue("Table is not exists", adminTemplate.tableExists(TABLE_NAME));
        adminTemplate.dropTableIfExist(TABLE_NAME);
        adminTemplate.tableExists(TABLE_NAME);

        Assert.assertFalse("Table is not dropped", adminTemplate.tableExists(TABLE_NAME));
    }

    @Test
    public void insertRow() {
        HTableDescriptor desc = new HTableDescriptor(TABLE_NAME);
        desc.addFamily(new HColumnDescriptor(COLUMN_FAMILY));
        adminTemplate.createTableIfNotExist(desc);
        Assert.assertTrue("Table is not exists", adminTemplate.tableExists(TABLE_NAME));

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

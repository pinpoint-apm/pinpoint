package com.navercorp.pinpoint.plugin.hbase.interceptor.util;

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.plugin.hbase.HbaseVersion;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class HbaseTableNameProviderFactoryTest {
    @Test
    public void testNewHbaseTableProvider() {

        HbaseTableNameProvider nameProvider = HbaseTableNameProviderFactory.getTableNameProvider(HbaseVersion.HBASE_VERSION_1);

        // Select hbase 1.x Implementation
        Assert.assertSame(Hbase1TableNameProvider.class, nameProvider.getClass());

        Table table = mock(Table.class);
        when(table.getName()).thenReturn(TableName.valueOf("testTable"));

        Assert.assertEquals("testTable", nameProvider.getName(table));
    }

    @Test
    public void testHbase2tableName() {
        HbaseTableNameProvider nameProvider = new Hbase1TableNameProvider();

        Table table = mock(Table.class);
        when(table.getName()).thenReturn(TableName.valueOf("testTable"));

        Assert.assertEquals("testTable", nameProvider.getName(table));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testHbase1tableName() {
        HbaseTableNameProvider nameProvider = new Hbase0TableNameProvider();

        HTableInterface table = mock(HTableInterface.class);
        when(table.getTableName()).thenReturn(BytesUtils.toBytes("testTable"));

        Assert.assertEquals("testTable", nameProvider.getName(table));
    }
}
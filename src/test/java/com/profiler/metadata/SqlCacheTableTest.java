package com.profiler.metadata;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Random;

/**
 *
 */
public class SqlCacheTableTest {
    @Test
    public void testPut() throws Exception {
        int cacheSize = 100;
        SqlCacheTable sqlCacheTable = new SqlCacheTable(cacheSize);
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            sqlCacheTable.put(new SqlObject(String.valueOf(random.nextInt(100000))));
        }

        int size = sqlCacheTable.getSize();
        Assert.assertEquals(size, cacheSize);

    }

    @Test
    public void testGetSize() throws Exception {
        SqlCacheTable sqlCacheTable = new SqlCacheTable(2);
        Assert.assertEquals(sqlCacheTable.getSize(), 0);

        SqlObject sqlObject = new SqlObject("test");

        boolean hit = sqlCacheTable.put(sqlObject);
        Assert.assertTrue(hit);
        Assert.assertEquals(sqlCacheTable.getSize(), 1);

        boolean hit2 = sqlCacheTable.put(sqlObject);
        Assert.assertFalse(hit2);
        Assert.assertEquals(sqlCacheTable.getSize(), 1);
//        "23 123";
//        "DCArMlhwQO 7"
        sqlCacheTable.put(new SqlObject("23 123"));
        sqlCacheTable.put(new SqlObject("DCArMlhwQO 7"));
        sqlCacheTable.put(new SqlObject("3"));
        sqlCacheTable.put(new SqlObject("4"));
        Assert.assertEquals(sqlCacheTable.getSize(), 2);


    }
}

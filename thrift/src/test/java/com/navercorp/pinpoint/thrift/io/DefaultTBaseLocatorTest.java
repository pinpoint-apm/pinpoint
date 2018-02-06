package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by suny on 2018/2/6.
 */
public class DefaultTBaseLocatorTest {
    DefaultTBaseLocator defaultTBaseLocator = new DefaultTBaseLocator();

    @Test
    public void tBaseLookup() throws Exception {
        TBase tBase = defaultTBaseLocator.tBaseLookup((short)66);
        boolean flag = false;
        if(tBase instanceof TBusinessLogBatch)
            flag =true;
        Assert.assertEquals("tBase instanceof TBusinessLogBatch ",flag,true);
    }

    @Test
    public void headerLookup() throws Exception {
        TBusinessLogBatch tBusinessLogBatch = new TBusinessLogBatch();
        Header header = defaultTBaseLocator.headerLookup(tBusinessLogBatch);
        Assert.assertNotNull(header);
    }

    @Test
    public void isSupport() throws Exception {
        boolean flag = defaultTBaseLocator.isSupport(TBusinessLogBatch.class);
        Assert.assertEquals(" support TBusinessLogBatch ",flag,true);
    }



}
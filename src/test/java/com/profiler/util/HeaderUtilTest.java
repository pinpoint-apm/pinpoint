package com.profiler.util;

import com.profiler.common.dto.Header;
import com.profiler.common.util.HeaderUtil;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderUtilTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Test
    public void validateSignature() throws TException {
        Header header = new Header();
        Assert.assertTrue(HeaderUtil.validateSignature(header.getSignature()));


        Header error = new Header((byte)0x11, (byte)0x20, (short)1);
        Assert.assertTrue(!HeaderUtil.validateSignature(error.getSignature()));


        logger.info(header.toString());
    }

}

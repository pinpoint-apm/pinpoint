package com.profiler.util;

import com.profiler.dto.*;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Logger;

public class HeaderUtilTest {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Test
    public void validateSignature() throws TException {
        Header header = new Header();
        Assert.assertTrue(HeaderUtil.validateSignature(header.getSignature()));


        Header error = new Header((byte)0x11, (byte)0x20, (short)1);
        Assert.assertTrue(!HeaderUtil.validateSignature(error.getSignature()));


        logger.info(header.toString());
    }

}

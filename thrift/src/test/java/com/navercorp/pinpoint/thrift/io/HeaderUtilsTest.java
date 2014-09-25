package com.nhn.pinpoint.thrift.io;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class HeaderUtilsTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Test
    public void validateSignature() throws TException {
        Header header = new Header();
        Assert.assertTrue(HeaderUtils.validateSignature(header.getSignature()) == HeaderUtils.OK);


        Header error = new Header((byte) 0x11, (byte) 0x20, (short) 1);
        Assert.assertTrue(HeaderUtils.validateSignature(error.getSignature()) != HeaderUtils.OK);


        logger.info(header.toString());
    }

}

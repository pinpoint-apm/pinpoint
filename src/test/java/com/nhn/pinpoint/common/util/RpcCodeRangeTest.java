package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class RpcCodeRangeTest {

    @Test
    public void testIsRpcRange() throws Exception {
        Assert.assertTrue(RpcCodeRange.isRpcRange(RpcCodeRange.RPC_START));
        Assert.assertTrue(RpcCodeRange.isRpcRange((short) (RpcCodeRange.RPC_END - 1)));
        Assert.assertFalse(RpcCodeRange.isRpcRange((short) 1));
    }

}
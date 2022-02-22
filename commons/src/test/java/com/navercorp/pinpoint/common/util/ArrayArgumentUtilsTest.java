package com.navercorp.pinpoint.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayArgumentUtilsTest {

    @Test
    public void getTest() {
        Object[] args = {"0", "1"};
        String arg0 = ArrayArgumentUtils.getArgument(args, 0, String.class);
        assertEquals("0", arg0);
        String arg1 = ArrayArgumentUtils.getArgument(args, 1, String.class);
        assertEquals("1", arg1);
    }

    @Test
    public void getTest_array() {
        Object[] args = {new Object[] {"0-0"}, new Object[] {"1-0"} };

        Object[] arg0 = ArrayArgumentUtils.getArgument(args, 0, Object[].class);
        assertArrayEquals(new Object[] {"0-0"}, arg0);
    }

    @Test
    public void getTest_typecast() {
        Object[] args = {"0", 1};
        String arg0 = ArrayArgumentUtils.getArgument(args, 0, String.class);
        assertEquals("0", arg0);
        Integer arg1 = ArrayArgumentUtils.getArgument(args, 1, Integer.class);
        assertEquals(1, arg1.intValue());
    }

    @Test
    public void getTest_typecast_error() {
        Object[] args = {"0", 1};
        String arg0 = ArrayArgumentUtils.getArgument(args, 0, String.class);
        assertEquals("0", arg0);
        String arg1 = ArrayArgumentUtils.getArgument(args, 1, String.class);
        assertNull(arg1);
    }


    @Test
    public void getTest_NPE() {
        Object[] args = null;
        String arg2 = ArrayArgumentUtils.getArgument(args, 0, String.class);
        assertNull(arg2);
    }

    @Test
    public void getTest_negative() {
        Object[] args = {"0", "1"};
        String arg2 = ArrayArgumentUtils.getArgument(args, -1, String.class);
        assertNull(arg2);
    }

    @Test
    public void getTest_OOB() {
        Object[] args = {"0", "1"};
        String arg2 = ArrayArgumentUtils.getArgument(args, 2, String.class);
        assertNull(arg2);
    }
}
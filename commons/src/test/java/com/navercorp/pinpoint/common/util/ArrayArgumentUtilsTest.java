package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArrayArgumentUtilsTest {

    @Test
    public void getArgument() {
        Object[] args = {"0", "1"};
        String arg0 = ArrayArgumentUtils.getArgument(args, 0, String.class);
        assertEquals("0", arg0);
        String arg1 = ArrayArgumentUtils.getArgument(args, 1, String.class);
        assertEquals("1", arg1);
    }

    @Test
    public void getArgument_array() {
        Object[] args = {new Object[]{"0-0"}, new Object[]{"1-0"}};

        Object[] arg0 = ArrayArgumentUtils.getArgument(args, 0, Object[].class);
        assertArrayEquals(new Object[]{"0-0"}, arg0);
    }

    @Test
    public void getArgument_typecast() {
        Object[] args = {"0", 1};
        String arg0 = ArrayArgumentUtils.getArgument(args, 0, String.class);
        assertEquals("0", arg0);
        Integer arg1 = ArrayArgumentUtils.getArgument(args, 1, Integer.class);
        assertEquals(1, arg1.intValue());
    }

    @Test
    public void getArgument_typecast_error() {
        Object[] args = {"0", 1};
        String arg0 = ArrayArgumentUtils.getArgument(args, 0, String.class);
        assertEquals("0", arg0);
        String arg1 = ArrayArgumentUtils.getArgument(args, 1, String.class);
        assertNull(arg1);
    }

    @Test
    public void getArgument_NPE() {
        Object[] args = null;
        String arg2 = ArrayArgumentUtils.getArgument(args, 0, String.class);
        assertNull(arg2);
    }

    @Test
    public void getArgument_negative() {
        Object[] args = {"0", "1"};
        String arg2 = ArrayArgumentUtils.getArgument(args, -1, String.class);
        assertNull(arg2);
    }

    @Test
    public void getArgument_OOB() {
        Object[] args = {"0", "1"};
        String arg2 = ArrayArgumentUtils.getArgument(args, 2, String.class);
        assertNull(arg2);
    }

    @Test
    public void getArgument_empty() {
        Object[] args = {};
        Object arg0 = ArrayArgumentUtils.getArgument(args, 0, Object.class);
        assertNull(arg0);
    }

    @Test
    public void getArgument_null() {
        Object[] args = {null};
        Object arg0 = ArrayArgumentUtils.getArgument(args, 0, Object.class, "empty");
        assertEquals("empty", arg0);
    }
}
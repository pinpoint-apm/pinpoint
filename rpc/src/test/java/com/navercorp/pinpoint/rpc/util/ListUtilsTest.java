package com.navercorp.pinpoint.rpc.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class ListUtilsTest {

    @Test
    public void addIfValueNotNullTest() {
        List<String> list = new ArrayList<String>();
        
        ListUtils.addIfValueNotNull(list, "firstString");
        ListUtils.addIfValueNotNull(list, null);
        
        Assert.assertEquals(1, list.size());
    }
    
    @Test
    public void addAllIfAllValuesNotNullTest() {
        List<String> list = new ArrayList<String>();

        String[] values = {"firstString", null, "secondString"};
        
        ListUtils.addAllIfAllValuesNotNull(list, values);
        
        Assert.assertEquals(0, list.size());
    }
    
    @Test
    public void addAllExceptNullValueTest() {
        List<String> list = new ArrayList<String>();

        String[] values = {"firstString", null, "secondString"};
        
        ListUtils.addAllExceptNullValue(list, values);
        
        Assert.assertEquals(2, list.size());
    }
    
}

package com.navercorp.pinpoint.web.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ListListUtilsTest {

    @Test
    public void toList() {

        List<String> a = Arrays.asList("a", "1");
        List<String> b = Arrays.asList("b", "2");
        List<List<String>> listList = new ArrayList<>();
        listList.add(a);
        listList.add(b);

        List<String> sum = ListListUtils.toList(listList);
        Assert.assertEquals(4, sum.size());
    }
}

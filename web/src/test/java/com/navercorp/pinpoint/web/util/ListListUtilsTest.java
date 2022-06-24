package com.navercorp.pinpoint.web.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(4, sum.size());
    }
}

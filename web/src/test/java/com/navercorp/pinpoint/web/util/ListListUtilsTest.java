package com.navercorp.pinpoint.web.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ListListUtilsTest {

    @Test
    public void toList() {

        List<String> a = List.of("a", "1");
        List<String> b = List.of("b", "2");

        List<List<String>> listList = List.of(a, b);

        List<String> sum = ListListUtils.toList(listList);
        assertThat(sum).hasSize(4);
    }
}

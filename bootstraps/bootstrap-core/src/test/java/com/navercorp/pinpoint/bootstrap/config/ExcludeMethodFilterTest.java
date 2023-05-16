package com.navercorp.pinpoint.bootstrap.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExcludeMethodFilterTest {

    @Test
    public void testFilter() {
        Filter<String> filter = new ExcludeMethodFilter("get,post");

        boolean getResult = filter.filter("GET");
        assertTrue(getResult);

        boolean postResult = filter.filter("POST");
        assertTrue(postResult);
    }

    @Test
    public void testUnFilter() {
        Filter<String> filter = new ExcludeMethodFilter("get,post");

        boolean putResult = filter.filter("PUT");
        assertFalse(putResult);

        boolean headResult = filter.filter("HEAD");
        assertFalse(headResult);
    }
}
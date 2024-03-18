package com.navercorp.pinpoint.bootstrap.plugin.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpStatusUtilsTest {

    @Test
    public void isError() {
        Assertions.assertTrue(HttpStatusUtils.isError(HttpStatus.SC_CLIENT_ERROR));
    }

    @Test
    public void isNonError() {
        Assertions.assertTrue(HttpStatusUtils.isNonError(HttpStatus.SC_SUCCESS));
    }
}
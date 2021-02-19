package com.navercorp.pinpoint.bootstrap.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Naver on 2015-11-17.
 */
public class InterceptorUtilsTest {

    @Test
    public void getHttpUrl() {
        assertEquals("/", InterceptorUtils.getHttpUrl("/", true));
        assertEquals("/", InterceptorUtils.getHttpUrl("/", false));

        assertEquals("/pinpoint.get?foo=bar", InterceptorUtils.getHttpUrl("/pinpoint.get?foo=bar", true));
        assertEquals("/pinpoint.get", InterceptorUtils.getHttpUrl("/pinpoint.get?foo=bar", false));


        assertEquals("http://google.com?foo=bar", InterceptorUtils.getHttpUrl("http://google.com?foo=bar", true));
        assertEquals("http://google.com", InterceptorUtils.getHttpUrl("http://google.com?foo=bar", false));

        assertEquals("http://google.com?foo=bar", InterceptorUtils.getHttpUrl("http://google.com?foo=bar", true));
        assertEquals("http://google.com", InterceptorUtils.getHttpUrl("http://google.com?foo=bar", false));

        assertEquals("https://google.com#foo", InterceptorUtils.getHttpUrl("https://google.com#foo", true));
        assertEquals("https://google.com#foo", InterceptorUtils.getHttpUrl("https://google.com#foo", false));
    }
}
package com.navercorp.pinpoint.web.service;

import org.junit.Test;

import static org.junit.Assert.*;

public class LinkDataLimiterTest {

    @Test
    public void excess() {
        LinkDataLimiter linkDataLimiter = new LinkDataLimiter();
        assertTrue(linkDataLimiter.excess(1000));
    }

}
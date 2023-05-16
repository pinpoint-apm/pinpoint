package com.navercorp.pinpoint.web.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkDataLimiterTest {

    @Test
    public void excess() {
        LinkDataLimiter linkDataLimiter = new LinkDataLimiter();
        assertTrue(linkDataLimiter.excess(1000));
    }

}
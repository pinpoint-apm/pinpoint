package com.navercorp.pinpoint.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LinkDataLimiter {

    @Value("${web.servermap.linkData.limit:500000000}")
    private long permitCount;

    public LinkDataLimiter() {
    }

    public boolean excess(long totalCount) {
        if (-1 != permitCount && totalCount > permitCount) { // Exceed the limit
            return true;
        }
        return false;
    }

    public String toString(long linkDataCount) {
        return linkDataCount + "/" + permitCount;
    }
}

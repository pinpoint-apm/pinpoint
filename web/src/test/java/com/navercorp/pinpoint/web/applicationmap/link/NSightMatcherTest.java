package com.navercorp.pinpoint.web.applicationmap.link;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.web.applicationmap.link.NSightMatcher;
import com.navercorp.pinpoint.web.applicationmap.link.ServerMatcher;

public class NSightMatcherTest {

    @Test
    public void success() {
        String sampleString = "dev-pinpoint-workload003.ncl.nhnsystem.com";

        ServerMatcher matcher = new NSightMatcher();

        Assert.assertTrue(matcher.isMatched(sampleString));
        String link = matcher.getLink(sampleString);
        Assert.assertEquals("http://nsight.nhncorp.com/dashboard_server/dev-pinpoint-workload003.ncl", link);
    }

}
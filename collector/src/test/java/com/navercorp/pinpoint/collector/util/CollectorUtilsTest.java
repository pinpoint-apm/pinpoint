package com.navercorp.pinpoint.collector.util;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;


public class CollectorUtilsTest {

    @Test
    public void getServerIdentifier() {

        String serverIdentifier = CollectorUtils.getHumanFriendlyServerIdentifier();

        String pid = String.valueOf(ProcessHandle.current().pid());
        MatcherAssert.assertThat(serverIdentifier, Matchers.endsWith(pid));
    }
}
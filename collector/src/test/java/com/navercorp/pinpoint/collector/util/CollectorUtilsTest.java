package com.navercorp.pinpoint.collector.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectorUtilsTest {

    @Test
    public void getServerIdentifier() {

        String serverIdentifier = CollectorUtils.getHumanFriendlyServerIdentifier();

        String pid = String.valueOf(ProcessHandle.current().pid());
        assertThat(serverIdentifier).endsWith(pid);
    }
}
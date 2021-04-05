package com.navercorp.pinpoint.plugin.jdbc.oracle;

import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

public class OracleContainerWithWait extends OracleContainer {
    public OracleContainerWithWait(String dockerImageVersion) {
        super(dockerImageVersion);
    }

    @Override
    protected void waitUntilContainerStarted() {
        // wait for Oracle to be fully initialized
        WaitStrategy waitStrategy = getWaitStrategy();
        if (waitStrategy != null) {
            waitStrategy.waitUntilReady(this);
        }

        // now, the JDBC connection should definitely work without lots of wasteful polling
        super.waitUntilContainerStarted();
    }
}

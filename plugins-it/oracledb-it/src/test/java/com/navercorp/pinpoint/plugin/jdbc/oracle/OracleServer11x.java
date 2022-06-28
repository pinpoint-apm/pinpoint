package com.navercorp.pinpoint.plugin.jdbc.oracle;

import org.testcontainers.containers.wait.strategy.Wait;

public class OracleServer11x extends OracleServer {
    public OracleServer11x() {
        super(OracleITConstants.ORACLE_11_X_IMAGE, Wait.defaultWaitStrategy());
    }

}

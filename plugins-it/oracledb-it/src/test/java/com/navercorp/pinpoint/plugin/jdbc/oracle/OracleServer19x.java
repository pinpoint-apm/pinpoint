package com.navercorp.pinpoint.plugin.jdbc.oracle;

import org.testcontainers.containers.wait.strategy.Wait;

public class OracleServer19x extends OracleServer {
    public OracleServer19x() {
        super(OracleITConstants.ORACLE_18_X_IMAGE, Wait.forLogMessage(".*Completed.*", 1));
    }

}

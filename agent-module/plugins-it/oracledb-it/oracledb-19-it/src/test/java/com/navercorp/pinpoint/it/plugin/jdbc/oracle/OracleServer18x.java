package com.navercorp.pinpoint.it.plugin.jdbc.oracle;

import org.testcontainers.containers.wait.strategy.Wait;

public class OracleServer18x extends OracleServer {
    public OracleServer18x() {
        super(OracleITConstants.ORACLE_18_X_IMAGE, Wait.forLogMessage(".*Completed.*", 1));
    }

}

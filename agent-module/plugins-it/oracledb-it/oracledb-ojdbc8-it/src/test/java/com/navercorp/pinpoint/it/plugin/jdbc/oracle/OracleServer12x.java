package com.navercorp.pinpoint.it.plugin.jdbc.oracle;

import org.testcontainers.containers.wait.strategy.Wait;

public class OracleServer12x extends OracleServer {
    public OracleServer12x() {
        super(OracleITConstants.ORACLE_18_X_IMAGE, Wait.forLogMessage(".*DATABASE IS READY TO USE.*\\n", 1));
    }

}

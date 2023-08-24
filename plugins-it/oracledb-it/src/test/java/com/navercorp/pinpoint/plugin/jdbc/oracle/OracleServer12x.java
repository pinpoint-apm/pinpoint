package com.navercorp.pinpoint.plugin.jdbc.oracle;

import org.testcontainers.containers.wait.strategy.Wait;

public class OracleServer12x extends OracleServer {
    public OracleServer12x() {
        super(OracleITConstants.ORACLE_12_X_IMAGE, Wait.forLogMessage(".*Database ready to use.*\\n", 1));
    }

}

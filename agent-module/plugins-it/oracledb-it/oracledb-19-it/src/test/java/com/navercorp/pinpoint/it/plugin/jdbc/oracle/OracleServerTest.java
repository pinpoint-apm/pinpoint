package com.navercorp.pinpoint.it.plugin.jdbc.oracle;

import org.junit.jupiter.api.Test;

public class OracleServerTest {
    @Test
    public void test() {
        OracleServer19x oracleServer19x = new OracleServer19x();
        oracleServer19x.beforeAll();
    }
}

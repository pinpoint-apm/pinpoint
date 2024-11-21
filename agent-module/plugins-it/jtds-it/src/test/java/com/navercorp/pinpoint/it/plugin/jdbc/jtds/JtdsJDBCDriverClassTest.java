package com.navercorp.pinpoint.it.plugin.jdbc.jtds;

import org.junit.jupiter.api.Test;

import java.sql.Driver;
import java.sql.SQLException;

class JtdsJDBCDriverClassTest {
    @Test
    void name() throws SQLException {
        JtdsJDBCDriverClass driverClass = new JtdsJDBCDriverClass();
        Driver driver = driverClass.newDriver();
        System.out.println(driver);
    }
}
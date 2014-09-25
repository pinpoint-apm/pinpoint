package com.nhn.pinpoint.testweb;

import oracle.jdbc.driver.OracleDriver;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 */
public class OracleErrorStackTraceCapture {

//    @Test
    public void errorConnect() throws SQLException {
        String url = "jdbc:oracle:thin:@10.98.133.173:1725:INDEV";
        Properties properties = new Properties();

        properties.setProperty("user", "lucy1");
        properties.setProperty("password", "lucy");

        OracleDriver oracleDriver = new OracleDriver();

        oracleDriver.connect(url, properties);


//        DriverManager.getConnection(url, "lucy", "lucy");



    }
}

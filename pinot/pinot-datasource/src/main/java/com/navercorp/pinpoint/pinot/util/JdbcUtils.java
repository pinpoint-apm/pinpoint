package com.navercorp.pinpoint.pinot.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public final class JdbcUtils {
    private static final Logger logger = LogManager.getLogger(JdbcUtils.class);

    private JdbcUtils() {
    }

    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                logger.debug("Could not close JDBC Connection", ex);
            } catch (Throwable ex) {
                logger.debug("Unexpected exception on closing JDBC Connection", ex);
            }
        }
    }
}

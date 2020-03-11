package com.navercorp.pinpoint.pluginit.jdbc.template;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementSetter {
    PreparedStatementSetter EMPTY = new PreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps) throws SQLException {

        }
    };

    void setValues(PreparedStatement ps) throws SQLException;
}

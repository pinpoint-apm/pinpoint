package com.navercorp.pinpoint.pluginit.jdbc.template;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface CallableStatementCallback<T> {
    T doInCallableStatement(CallableStatement var1) throws SQLException;
}

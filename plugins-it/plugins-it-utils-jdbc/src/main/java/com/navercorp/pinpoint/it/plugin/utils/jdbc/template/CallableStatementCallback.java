package com.navercorp.pinpoint.it.plugin.utils.jdbc.template;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface CallableStatementCallback<T> {
    T doInCallableStatement(CallableStatement var1) throws SQLException;
}

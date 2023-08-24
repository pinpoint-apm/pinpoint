package com.navercorp.pinpoint.pluginit.jdbc.template;

import java.sql.SQLException;

public interface TransactionCallback {
     void doInTransaction() throws SQLException;
}

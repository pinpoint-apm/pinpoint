package com.navercorp.pinpoint.it.plugin.utils.jdbc.template;

import java.sql.SQLException;

public interface TransactionCallback {
     void doInTransaction() throws SQLException;
}

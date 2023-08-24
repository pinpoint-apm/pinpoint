package com.navercorp.pinpoint.it.plugin.utils.jdbc.template;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetExtractor<T> {
    T extractData(ResultSet rs) throws SQLException;
}

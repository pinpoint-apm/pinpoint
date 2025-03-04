package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

public interface JdbcOption {

    static JdbcOption empty() {
        return new DefaultJdbcOption();
    }

    int getJdbcSqlCacheSize();

    boolean isTraceSqlBindValue();

    int getMaxSqlBindValueSize();

    int getMaxSqlCacheLength();

    int getMaxSqlLength();

    boolean isRemoveComments();
}

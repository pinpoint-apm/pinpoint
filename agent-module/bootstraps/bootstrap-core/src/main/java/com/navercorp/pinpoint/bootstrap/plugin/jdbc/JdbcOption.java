package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

public interface JdbcOption {

    static JdbcOption empty() {
        return new DefaultJdbcOption();
    }

    int getJdbcSqlCacheSize();

    int getMaxSqlCacheLength();

    long getSqlCacheExpireHours();

    boolean isTraceSqlBindValue();

    int getMaxSqlBindValueSize();

    int getMaxSqlLength();

    boolean isRemoveComments();
}

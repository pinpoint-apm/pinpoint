package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.common.config.Value;

public class DefaultJdbcOption implements JdbcOption {
    @Value("${profiler.jdbc.sqlcachesize}")
    private int jdbcSqlCacheSize = 1024;
    @Value("${profiler.jdbc.tracesqlbindvalue}")
    private boolean traceSqlBindValue = false;
    @Value("${profiler.jdbc.maxsqlbindvaluesize}")
    private int maxSqlBindValueSize = 1024;
    @Value("${profiler.jdbc.sqlcachelengthlimit}")
    private int maxSqlCacheLength = 2048;
    @Value("${profiler.jdbc.maxsqllength}")
    private int maxSqlLength = 65536;
    @Value("${profiler.jdbc.removecomments}")
    private boolean removeComments = true;

    public DefaultJdbcOption() {
    }

    public DefaultJdbcOption(
            int jdbcSqlCacheSize,
            boolean traceSqlBindValue,
            int maxSqlBindValueSize,
            int maxSqlCacheLength,
            int maxSqlLength,
            boolean removeComments) {
        this.jdbcSqlCacheSize = jdbcSqlCacheSize;
        this.traceSqlBindValue = traceSqlBindValue;
        this.maxSqlBindValueSize = maxSqlBindValueSize;
        this.maxSqlCacheLength = maxSqlCacheLength;
        this.maxSqlLength = maxSqlLength;
        this.removeComments = removeComments;
    }

    @Override
    public int getJdbcSqlCacheSize() {
        return jdbcSqlCacheSize;
    }

    @Override
    public boolean isTraceSqlBindValue() {
        return traceSqlBindValue;
    }

    @Override
    public int getMaxSqlBindValueSize() {
        return maxSqlBindValueSize;
    }

    @Override
    public int getMaxSqlCacheLength() {
        return maxSqlCacheLength;
    }

    @Override
    public int getMaxSqlLength() {
        return maxSqlLength;
    }

    @Override
    public boolean isRemoveComments() {
        return removeComments;
    }

    @Override
    public String toString() {
        return "DefaultJdbcOption{" +
                "jdbcSqlCacheSize=" + jdbcSqlCacheSize +
                ", traceSqlBindValue=" + traceSqlBindValue +
                ", maxSqlBindValueSize=" + maxSqlBindValueSize +
                ", maxSqlCacheLength=" + maxSqlCacheLength +
                ", maxSqlLength=" + maxSqlLength +
                ", removeComments=" + removeComments +
                '}';
    }
}

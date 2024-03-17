package com.navercorp.pinpoint.profiler.metadata;

public class SqlUidMetaData implements MetaDataType {
    private final byte[] sqlUid;
    private final String sql;

    public SqlUidMetaData(byte[] sqlUid, String sql) {
        this.sqlUid = sqlUid;
        this.sql = sql;
    }

    public byte[] getSqlUid() {
        return sqlUid;
    }

    public String getSql() {
        return sql;
    }
}

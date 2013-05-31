package com.nhn.pinpoint.metadata;

import com.nhn.pinpoint.util.Assert;

/**
 * 없애도 될듯하다.
 */
@Deprecated
public class SqlObject {
    private String parsedSql;

    public SqlObject(String parsedSql) {
        Assert.notNull(parsedSql, "parsedSql is not null");
        this.parsedSql = parsedSql;
    }

    public String getParsedSql() {
        return parsedSql;
    }

    public int getParsedSqlHashCode() {
        return parsedSql.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SqlObject sqlObject = (SqlObject) o;

        if (parsedSql != null ? !parsedSql.equals(sqlObject.parsedSql) : sqlObject.parsedSql != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parsedSql != null ? parsedSql.hashCode() : 0;
    }
}

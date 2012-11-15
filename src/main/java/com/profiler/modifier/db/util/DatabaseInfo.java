package com.profiler.modifier.db.util;

/**
 *
 */
public class DatabaseInfo {
    public enum DBType {
        ORACLE, MYSQL, MSSQL, CUBRID, UNKOWN
    }

    DBType type = DBType.UNKOWN;
    String databaseId;
    String url;
    String host;
    String port;


    public DatabaseInfo(DBType type, String url, String host, String port, String databaseId) {
        this.type = type;
        this.url = url;
        this.host = host;
        this.port = port;
        this.databaseId = databaseId;
    }

    @Deprecated
    public String getHost() {
        // host와 port의 경우 replication 설정등으로 n개가 될수 있어 애매하다.
        return host;
    }

    @Deprecated
    public String getPort() {
        // host와 port의 경우 replication 설정등으로 n개가 될수 있어 애매하다.
        return port;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public String getUrl() {
        return url;
    }

    public DBType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DatabaseInfo{" +
                "type=" + type +
                ", databaseId='" + databaseId + '\'' +
                ", url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}

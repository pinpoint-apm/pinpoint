package com.pinpoint.test.plugin;

public class MysqlDBServer {
    private static final int PORT = 32772;

    public static String getUri() {
        return "jdbc:mysql://localhost:" + PORT + "/test?serverTimezone=UTC&useSSL=false";
    }
}

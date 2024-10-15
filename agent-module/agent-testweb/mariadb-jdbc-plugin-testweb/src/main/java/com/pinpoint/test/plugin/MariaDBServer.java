package com.pinpoint.test.plugin;

public class MariaDBServer {
    private static final int PORT = 33001;

    public static String getUri() {
        return "jdbc:mariadb://localhost:" + PORT + "/test";
    }

}

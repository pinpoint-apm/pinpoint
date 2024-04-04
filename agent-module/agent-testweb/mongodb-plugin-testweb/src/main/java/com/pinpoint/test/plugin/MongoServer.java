package com.pinpoint.test.plugin;

public class MongoServer {
    private static final int PORT = 62449;

    public static String getUri() {
        return "mongodb://localhost:" + PORT;
    }

}

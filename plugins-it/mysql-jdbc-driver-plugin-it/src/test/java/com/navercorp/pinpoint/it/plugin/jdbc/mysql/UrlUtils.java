package com.navercorp.pinpoint.it.plugin.jdbc.mysql;

public final class UrlUtils {
    public static String getLoadbalanceUrl(String url) {
        return url.replace("jdbc:mysql://", "jdbc:mysql:loadbalance://");
    }
}

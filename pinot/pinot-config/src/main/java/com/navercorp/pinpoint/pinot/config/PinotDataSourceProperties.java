package com.navercorp.pinpoint.pinot.config;

public class PinotDataSourceProperties {
    private String jdbcUrl;
    private String username;
    private String password;
    private String brokers;

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBrokers(String brokers) {
        this.brokers = brokers;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBrokers() {
        return brokers;
    }

    @Override
    public String toString() {
        return "PinotDataSourceProperties{" +
                "jdbcUrl='" + jdbcUrl + '\'' +
                ", username='" + username + '\'' +
                ", brokers='" + brokers + '\'' +
                '}';
    }
}

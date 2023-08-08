package com.navercorp.pinpoint.pinot.config;

public class PinotDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String brokers;

    public void setUrl(String url) {
        this.url = url;
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

    public String getUrl() {
        return url;
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
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

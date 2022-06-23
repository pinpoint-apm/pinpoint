package com.navercorp.pinpoint.plugin.spring.web;

import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;

import java.util.Properties;

public class HttpWebServer implements SharedTestLifeCycle {
    private WebServer webServer;

    @Override
    public Properties beforeAll() {
        try {
            webServer = WebServer.newTestWebServer();

            Properties properties = new Properties();
            properties.setProperty("HOST_PORT", webServer.getHostAndPort());
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("webserver start error", e);
        }
    }

    @Override
    public void afterAll() {
        webServer = WebServer.cleanup(webServer);
    }
}
